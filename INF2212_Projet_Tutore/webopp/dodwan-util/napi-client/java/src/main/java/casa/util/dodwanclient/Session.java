package casa.util.dodwanclient;

import casa.util.pdu.BsonPduSerializer;
import casa.util.pdu.Pdu;
import java.net.SocketException;

/**
* A session allowing to send/receive PDUs to/from a NAPI server. A session is
* identified by a clientId. PDUs are exchanged as soon as the session is started.
* When the session is stopped, in/outcomming PDUs are discarded (no buffering).
* The client status (subscription) is kept by the server until the {@link bye()}
* method invocation
*
* The session possible states are: STOPPED, STARTING, STARTING. Moreover the
* underlying connection can be connected or not
*
* <pre>
*   STOPPED    <---- stop() -------   STARTING
* DISCONNECTED ----- start() --+--> DISCONNECTED
*      ^                      /          |
*      |                     /           |
* stop()/bye()              /            |
*      |                   /     onConnectionOpened()
*      |    onConnectionClosed()         |
* +----+    /                 \          |
* |    |   /                   \         v
* |  STARTED  <------ hello() ---+--- STARTING
* | CONNECTED                         CONNECTED
* |                                      |
* +--------------------------------------+
* </pre>
*/
public class Session extends Listenable<SessionListener> implements ConnectionListener
{
    /**
    * The session possible states
    */
    enum SessionState {
        STOPPED,
        STARTING, // the session is currently trying to connect and send hello PDUs
        STARTED   // the session is started and ready to send/receive PDUs
    }

    /**
    * Counter to generate unique tokens for requests
    */
    private static int TKN_CPT = 0;

    /**
    * Singleton instance of this class
    */
    private static Session instance;

    /**
    * Timeout for acknoledgements
    */
    private final int ackTimeout;

    /**
    * the identifier of the client for this session
    */
    private final String clientId;

    /**
    * TCP connection used to send/receive PDUs
    */
    private final Connection connection;

    /**
    * maximum number of successive TCP connection attempts
    */
    private final int maxConnAttempts;

    /**
    * minimum delay between subsequent TCP connection attempts
    */
    private final int connAttemptsDelay;

    /**
    * minimum delay between subsequent ping messages emissions
    */
    private final int pingPongDelay;

    /**
    * the channel used to send/process pdus
    */
    private final PduChannel channel;

    /**
    * current number of successive TCP connection attempts
    */
    private int nbConnAttempts;

    /**
    * time of the last  TCP connection attempt
    */
    private long lastAttemptTime;

    /**
    * the current session state
    */
    private SessionState state;

    /**
    * true if the TCP connection is opened
    */
    private boolean connected;

    /**
    * true if the session has already been started and not closed with a bye
    */
    private boolean continuation;

    /**
    * Give a singleton instance of this class
    *
    * @return singleton {@link Session}
    */
    public static Session getInstance()
    {
        if (instance == null) {
            Configuration conf = Configuration.getInstance();
            Connection connection = new Connection(conf.getServerHost(), conf.getServerPort(), conf.getSerializer(), conf.getReadTimeout());
            instance = new Session(conf.getClientId(), connection, conf.getMaxConnAttempts(), conf.getConnAttemptsDelay(), conf.getPingPongDelay(), conf.getReplyTimeout());
        }
        return null;
    }

    /**
    * Constructor.
    *
    * @param clientId the identifier of the client for this session.
    * @param connection TCP connection used to send/receive PDUs
    * @param maxConnAttempts maximum number of successive TCP connection attempts
    * @param connAttemptsDelay minimum delay between subsequent TCP connection attempts
    * @param pingPongDelay minimum delay between subsequent ping messages emissions
    * @param ackTimeout the timeout for acknoledgements
    */
    public Session(String clientId, Connection connection, int maxConnAttempts, int connAttemptsDelay, int pingPongDelay, int ackTimeout)
    {
        // TODO generate clientId
        this.clientId = clientId;
        this.connection = connection;
        this.connection.addListener(this);
        this.channel = connection.getChannel();
        this.maxConnAttempts = maxConnAttempts;
        this.connAttemptsDelay = connAttemptsDelay;
        this.pingPongDelay = pingPongDelay;
        this.state = SessionState.STOPPED;
        this.ackTimeout = ackTimeout;
    }

    /**
    * Give the channel associated to this session used to send/receive PDUs
    *
    * @return the channel associated to this session
    */
    PduChannel getChannel()
    {
        return this.channel;
    }

    /**
    * Stop the session and send a bye PDU to the server.The server clears the
    * client status (subscriptions) for subsequent sessions.
    *
    * @throws Exception the "bye" PDU could have not been received by the server
    */
    public void bye() throws Exception
    {
        if (this.connected) {
            close(true);
        }
    }

    /**
    * Stop the session in order to stop sending/receiving PDUs. The server keeps
    * the client status (subscriptions) for subsequent starts of a session with
    * the same clientId
    *
    * @throws Exception the "stop" PDU could have not been received by the server
    */
    public void stop() throws Exception
    {
        if (this.state != SessionState.STOPPED) {
            close(false);
        }
    }

    /**
    * Start the session in order to send/receive PDUs. This method attempts to
    * establish a connection with the server, send "hello" and "start" PDUs and
    * generate a {@link SessionListener#onSessionStarted()} event if it succeeds.
    */
    public void start()
    {
        if (this.state == SessionState.STOPPED) {
            this.onSessionStarting();
            if (!this.connected) {
                this.connect();
            } else {
                this.hello();
            }
        }
    }

    //------------------------------------------------------------------------
    // Connection events
    //------------------------------------------------------------------------
    /**
    * Method called when the connection is opened
    */
    @Override
    public void onConnectionOpened()
    {
        switch (this.state) {
            case STARTING:
            this.connected = true;
            this.channel.open();
            for (SessionListener listener : getListeners()) {
                listener.onConnectionOpened();
            }
            this.hello();
            break;
            case STOPPED:
            this.connection.disconnect();
        }
    }

    /**
    * Method called when the connection is closed
    */
    @Override
    public void onConnectionClosed()
    {
        this.connected = false;
        this.channel.close();
        for (SessionListener listener : getListeners()) {
            listener.onConnectionClosed();
        }
        switch (this.state) {
            case STARTED:
            this.onSessionStarting();
            this.connect();
            break;
            case STARTING:
            if (this.nbConnAttempts < this.maxConnAttempts) {
                this.connect();
            }
            break;
        }
    }

    /**
    * Method called when an error occurs on the connection
    */
    @Override
    public void onConnectionError(Exception e)
    {
        for (SessionListener listener : getListeners()) {
            listener.onConnectionError(e);
        }
        if (e instanceof SocketException) {
            if (this.connected) {
                this.connection.disconnect();
            } else if (this.nbConnAttempts < this.maxConnAttempts) {
                this.connect();
            } else {
                this.onSessionStopped();
            }
        }
    }

    //------------------------------------------------------------------------
    // Private methods
    //------------------------------------------------------------------------
    /**
    * Try to open the TCP connection. Wait for an appropriate delay if needed
    */
    private void connect()
    {
        new Thread() {
            @Override
            public void run() {
                long delay = lastAttemptTime + connAttemptsDelay - System.currentTimeMillis();
                if (delay > 0) {
                    try { Thread.sleep(delay); } catch (Exception e) { }
                }
                nbConnAttempts++;
                lastAttemptTime = System.currentTimeMillis();
                connection.connect();
            }
        }.start();
    }

    /**
    * Send "hello" and "start" PDUs and start the session. The connection
    * must be opened. Set the session state as STARTED when the PDUs have been
    * successfully sent
    */
    private void hello()
    {
        Pdu reply = null;
        try {
            reply = this.writeAndRetry(this.makeHelloPdu(), ackTimeout, "ok", 1, 3);
            if (this.state == SessionState.STARTING && this.connected) {
                reply = this.writeAndRetry(this.makePdu("start"), ackTimeout, "ok", 1, 3);
                this.onSessionStarted();
            }
        } catch (UnexpectedPduException e) {
            this.connection.disconnect();
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            // DO NOTHING. Error events are processed elsewhere
        }
    }

    /**
    * Send the given PDU, and, if a UnexpectedPduException occurs, send it again
    * (maxRetry-retryCpt) times. Method implemented in a recursive way.
    *
    * @param pdu the PDU to be written
    * @param replyTimeout the timeout to wait for the reply
    * @param expectedReply the expected name of the reply PDU
    * @param retryCpt the number of attempts already done
    * @param maxRetry the maximum number of attempts
    * @return the received PDU at the last attempt. May be null, if the timeout expires
    */
    private Pdu writeAndRetry(Pdu pdu, int ackTimeout, String expectedReply, int retryCpt, int maxRetry) throws Exception
    {
        try {
            pdu.putString("tkn", pdu.getString("tkn") + ".");
            return this.channel.writeForReply(pdu, ackTimeout, "ok");
        } catch (UnexpectedPduException e) {
            if (retryCpt < maxRetry) {
                return writeAndRetry(pdu, ackTimeout, expectedReply, retryCpt+1, maxRetry);
            } else {
                throw e;
            }
        }
    }

    /**
    * Send close PDUs, stop the connection and stop the session. The connection
    * must be opened
    *
    * @param bye if true, a "bye" PDU is sent
    * @throws Exception the "stop" and "bye" PDUs could have not been received by the server
    */
    private void close(boolean bye) throws Exception
    {
        try {
            if (this.connected) {
                this.channel.writeForReply(this.makePdu("stop"), ackTimeout, "ok");
                if (bye) {
                    this.channel.writeForReply(this.makePdu("bye"), ackTimeout, "ok");
                }
            }
        } finally {
            this.onSessionStopped();
            if (this.connected) {
                try { this.connection.disconnect(); } catch (Exception e) { }
            }
        }
    }

    private void startPingPong()
    {
        int tknCpt = (int)System.currentTimeMillis();
        int errCpt = 0;
        Pdu pdu = this.channel.makePdu();
        pdu.putString("name", "ping");
        while (this.connected && errCpt < 3) {
            try {
                pdu.putString("tkn", "PING-" + tknCpt++);
                this.channel.writeForReply(pdu, ackTimeout, "pong");
                errCpt = 0;
                try { Thread.sleep(pingPongDelay); } catch (Exception e) { }
            } catch (Exception e) {
                errCpt++;
            }
        }
        if (this.connected) {
            try { this.connection.disconnect(); } catch (Exception e) { }
        }
    }

    //------------------------------------------------------------------------
    // Session states management
    //------------------------------------------------------------------------
    /**
    * Notify listeners that the session is starting and update the session status
    * accordingly
    */
    private void onSessionStarting()
    {
        this.nbConnAttempts = 0;
        this.state = SessionState.STARTING;
        for (SessionListener listener : getListeners()) {
            listener.onSessionStarting();
        }
    }

    /**
    * Notify listeners that the session is started and update the session status
    * accordingly
    */
    private void onSessionStarted()
    {
        this.state = SessionState.STARTED;
        this.continuation = true;
        for (SessionListener listener : getListeners()) {
            listener.onSessionStarted();
        }
    }

    /**
    * Notify listeners that the session is stopped and update the session status
    * accordingly
    */
    private void onSessionStopped()
    {
        this.state = SessionState.STOPPED;
        for (SessionListener listener : getListeners()) {
            listener.onSessionStopped();
        }
    }

    //------------------------------------------------------------------------
    // Control PDUs
    //------------------------------------------------------------------------
    private Pdu makeHelloPdu()
    {
        Pdu pdu = makePdu("hello");
        pdu.putString("client", this.clientId);
        pdu.putBoolean("cont", this.continuation);
        return pdu;
    }

    private Pdu makePdu(String name)
    {
        Pdu pdu = this.channel.makePdu();
        pdu.putString("tkn", "SESSION-" + TKN_CPT++);
        pdu.putString("name", name);
        return pdu;
    }
}
