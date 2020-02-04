package casa.util.dodwanclient;

import casa.util.pdu.BsonPduSerializer;
import casa.util.pdu.Pdu;
import java.net.SocketException;
import static org.junit.Assert.*;

/**
* "Fake" {@link Connection} object used to control and spy a {@link Connection} behavior
*/
public class ConnectionMock extends Connection
{
    private final ConnectionPduChannelMock channel;      // "fake" channel
    private boolean acceptConnect;
    private int acceptConnectAttempt, connectAttemptCpt;
    private boolean connected;
    private String[] expectedWritten;
    private int current;

    /**
    * Constructor
    */
    public ConnectionMock()
    {
        super("", 0, new BsonPduSerializer(), 0);
        this.channel = new ConnectionPduChannelMock();
    }

    /**
    * Constructor
    *
    * @param expectedWritten the names of the PDUs that should be written
    */
    public ConnectionMock(String[] expectedWritten)
    {
        this();
        this.expectedWritten = expectedWritten;
    }

    /**
    * Method to control the {@link Connection} behavior
    * @param acceptConnect true if the {@link connect()} method should succeed
    */
    public void setAcceptConnect(boolean acceptConnect)
    {
        this.acceptConnect = acceptConnect;
        this.acceptConnectAttempt = -1;
    }

    /**
    * Method to control the {@link Connection} behavior
    * @param attemptNb the {@link connect()} attempt invocation that should succeed
    * (after attemptNb-1 failures)
    */
    public void setAcceptConnectAttempt(int attemptNb)
    {
        if (attemptNb > 0) {
            this.acceptConnect = false;
            this.connectAttemptCpt = 0;
            this.acceptConnectAttempt = attemptNb;
        } else {
            setAcceptConnect(true);
        }
    }

    /**
    * Method to control the {@link Connection} behavior
    * @param reply the PDU that should be returned by the {@link Channel#writeForReply(Pdu, int)} method
    * @param replyDelay the delay for the {@link Channel#writeForReply(Pdu, int)} method to return a reply
    */
    public void setReply(Pdu reply, int replyDelay)
    {
        this.channel.setReply(reply, replyDelay);
    }

    /**
    * Method to observe the {@link Connection} behavior. Check that the expected
    * messages have been written
    */
    public void checkWritten()
    {
        assertEquals("Number of written PDUs incorrect", expectedWritten.length, channel.nbWritten);
    }

    @Override
    public PduChannel getChannel()
    {
        return this.channel;
    }

    @Override
    public void connect()
    {
        if (!this.connected) {
            this.connectAttemptCpt++;
            if (this.acceptConnect || (this.acceptConnectAttempt > 0 && this.connectAttemptCpt >= this.acceptConnectAttempt)) {
                this.onOpened();
            } else {
                this.onError(new SocketException(""));
            }
        }
    }

    @Override
    public void disconnect()
    {
        if (this.connected) {
            this.onClosed();
        }
    }

    private void onOpened()
    {
        this.connected = true;
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionOpened();
        }
    }

    private void onClosed()
    {
        this.connected = false;
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionClosed();
        }
    }

    public void onError(Exception e)
    {
        for (ConnectionListener listener : getListeners()) {
            listener.onConnectionError(e);
        }
    }

    /**
    * "Fake" {@link PduChannel} object used to control and spy a {@link PduChannel} behavior
    */
    class ConnectionPduChannelMock extends PduChannel
    {
        private boolean opened;
        private Pdu reply;
        private int replyDelay;
        private int nbWritten;

        public void setReply(Pdu reply, int replyDelay)
        {
            this.reply = reply;
            this.replyDelay = replyDelay;
        }

        @Override
        public void open() {
            this.opened = true;
        }

        @Override
        public void close() {
            this.opened = false;
        }

        @Override
        public void write(Pdu pdu) throws Exception
        {
            if (expectedWritten != null) {
                assertEquals("Unexpected pdu written", expectedWritten[current++], pdu.getString("name"));
                nbWritten++;
            }
            if (!ConnectionMock.this.connected) {
                throw new SocketException("");
            }
        }

        @Override
        public Pdu writeForReply(Pdu pdu, long replyTimeout) throws Exception
        {
            this.write(pdu);
            if (this.replyDelay > 0) {
                Thread.sleep(this.replyDelay);
            }
            return this.reply;
        }
    }
}
