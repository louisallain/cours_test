package casa.util.dodwanclient;

import casa.util.Future;
import casa.util.Processor;
import casa.util.pdu.Pdu;
import casa.util.pdu.PduSerializer;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* A connection represents a TCP connection with a server used to send/receive PDUs
* using JSON/BSON
*/
class Connection extends Listenable<ConnectionListener>
{
    /**
    * timeout for read operations
    */
    private final int readTimeout;

    /**
    * the server host name
    */
    private final String host;

    /**
    * the server port number
    */
    private final int port;

    /**
    * serializer used to exchange PDUs through this connection
    */
    private final PduSerializer serializer;

    /**
    * Channel to read/write PDUs through this TCP connection
    */
    private final PduChannel channel;

    /**
    * socket connected to the server if {@link #connected} is true
    */
    private Socket socket;

    /**
    * true if a TCP connection is opened with the server
    */
    private boolean connected;

    /**
    * Constructor
    *
    * @param host the server host name
    * @param port the server port number
    * @param serializer serializer used to exchange PDUs through this connection
    * @param readTimeout the timeout for read operations
    */
    public Connection(String host, int port, PduSerializer serializer, int readTimeout)
    {
        this.host = host;
        this.port = port;
        this.serializer = serializer;
        this.channel = new ConnectionPduChannel();
        this.readTimeout = readTimeout;
    }

    /**
    * Give a channel to read/write PDUs through this TCP connection
    *
    * @return the channel
    */
    public PduChannel getChannel()
    {
        return this.channel;
    }

    /**
    * Open a TCP connection with the server
    */
    public void connect()
    {
        if (!this.connected) {
            try {
                this.socket = new Socket(this.host, this.port);
                if (!this.socket.isConnected()) {
                    throw new SocketException("Socket no connected");
                }
                this.socket.setSoTimeout(this.readTimeout);
                this.onOpened();
            } catch (Exception e) {
                this.onError(e);
            }
        }
    }

    /**
    * Close the TCP connection with the server
    */
    public void disconnect()
    {
        if (this.connected) {
            this.onClosed();
            try { this.channel.close(); } catch (Exception e) { }
            try { this.socket.close(); } catch (Exception e) { }
        }
    }

    /**
    * Write a Pdu to the TCP connection. If the connection is closed, do nothing
    *
    * @param pdu the pdu to be written
    */
    private void write(Pdu pdu) throws Exception
    {
        if (this.connected) {
            try {
                this.serializer.write(pdu, this.socket.getOutputStream());
            } catch (Exception e) {
                this.onError(e);
                throw e;
            }
        }
    }

    /**
    * Read a Pdu from the TCP connection. If the connection is closed, do nothing
    *
    * @param pdu the pdu to be read
    */
    private void read(Pdu pdu) throws Exception
    {
        if (this.connected) {
            try {
                int size = this.serializer.read(pdu, this.socket.getInputStream());
                if (size < 0) {
                    throw new IOException("No PDU read from InputStream");
                }
            } catch (Exception e) {
                this.onError(e);
                throw e;
            }
        }
    }

    /**
    * Notify listeners that the connection is opened
    */
    private void onOpened()
    {
        this.connected = true;
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionOpened();
        }
    }

    /**
    * Notify listeners that the connection is closed
    */
    private void onClosed()
    {
        this.connected = false;
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionClosed();
        }
    }

    /**
    * Notify listeners that an error occured
    */
    private void onError(Exception e)
    {
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionError(e);
        }
    }

    /**
    * A {@link PduChannel} relying on a {@link Connection} to send/receive PDUs
    */
    class ConnectionPduChannel extends PduChannel
    {
        private final Pdu NO_REPLY_PDU = this.makePdu();

        private final Map<String, Future<Pdu>> replyListeners;

        private boolean opened;

        /**
        * Constructor
        */
        ConnectionPduChannel()
        {
            this.replyListeners = new ConcurrentHashMap<>();
        }

        @Override
        public void open()
        {
            if (!this.opened) {
                new Thread() {
                    @Override
                    public void run() {
                        opened = true;
                        while (opened && connected) {
                            try {
                                Pdu pdu = Connection.this.channel.makePdu();
                                Connection.this.read(pdu);
                                String token = pdu.getString("tkn");
                                String name = pdu.getString("name");
                                if (token != null) {
                                    Future<Pdu> replyListener = replyListeners.get(token);
                                    if (replyListener != null) {
                                        replyListener.putValue(pdu);
                                    }
                                } else if (name != null) {
                                    Processor<Pdu> processor = getProcessor(name);
                                    if (processor != null) {
                                        processor.process(pdu);
                                    }
                                }
                                Thread.sleep(1);
                            } catch (Exception e) {
                                close();
                                // e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }

        @Override
        public void close()
        {
            if (this.opened) {
                this.opened = false;
                for (String token : this.replyListeners.keySet()) {
                    Future<Pdu> replyListener = this.replyListeners.get(token);
                    replyListener.putValue(this.makePdu("error", token));
                }
            }
        }

        @Override
        public void write(Pdu pdu) throws Exception
        {
            Connection.this.write(pdu);
        }

        @Override
        public Pdu writeForReply(Pdu pdu, long replyTimeout) throws Exception
        {
            String token = pdu.getString("tkn");
            Future<Pdu> replyListener = new Future<>(replyTimeout, NO_REPLY_PDU);
            this.replyListeners.put(token, replyListener);
            this.write(pdu);
            Pdu reply = replyListener.getValue();
            this.replyListeners.remove(token, replyListener);
            if (reply != NO_REPLY_PDU) {
                return reply;
            }
            return null;
        }

        private Pdu makePdu(String name, String token)
        {
            Pdu pdu = Connection.this.channel.makePdu();
            pdu.putString("name", name);
            pdu.putString("tkn", token);
            return pdu;
        }
    }
}
