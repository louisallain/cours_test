package casa.util.dodwanclient;

import casa.util.pdu.BsonPduSerializer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
* Tests for the {@link Connection} class
*/
public class ConnectionTest
{
    private static final int CONN_OPENED = 0;
    private static final int CONN_CLOSED = 1;
    private static final int CONN_ERR = 2;

    private static int TEST_PORT = 12345;

    private Connection instance;
    private SessionListenerMock listener;

    /**
    * Tests for the {@link Connection#connect()} method
    */
    @Test
    public void testConnect() throws Exception
    {
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer())) {
            int[] expectedOK = { CONN_OPENED };
            connect(console, true, expectedOK);
        }

        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+2, new BsonPduSerializer())) {
            int[] expectedNOK = { CONN_ERR };
            connect(console, false, expectedNOK);
        }
    }

    /**
    * Tests for the {@link Connection#disconnect()} method
    */
    @Test
    public void testDisconnect() throws Exception
    {
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+3, new BsonPduSerializer())) {
            int[] expectedConnect = { CONN_OPENED };
            int[] expectedDisconnect = { CONN_CLOSED };
            connect(console, true, expectedConnect);

            setListener(expectedDisconnect);
            instance.disconnect();
            listener.checkEvents();
            Thread.sleep(10);
            assertFalse(console.isConnected());
        }
    }

    /**
    * Start the console and invoke the {@link Connection#connect()} method.
    * @param console the console to be started
    * @param acceptConnect true if the console should accept connections
    * @param expected the expected events
    */
    private void connect(ConsoleSocket console, boolean acceptConnect, int[] expected) throws Exception
    {
        if (acceptConnect) {
            console.start();
            Thread.sleep(100);
        }

        instance = new Connection("localhost", console.getPort(), new BsonPduSerializer(), 500);
        setListener(expected);
        instance.connect();
        listener.checkEvents();
        Thread.sleep(10);
        assertEquals(acceptConnect, console.isConnected());
    }

    /**
    * Set a new {@link SessionListener} with the given expected events
    */
    private void setListener(int[] expected)
    {
        if (listener != null) {
            instance.removeListener(listener);
        }
        listener = new SessionListenerMock(expected);
        instance.addListener(listener);
    }
}
