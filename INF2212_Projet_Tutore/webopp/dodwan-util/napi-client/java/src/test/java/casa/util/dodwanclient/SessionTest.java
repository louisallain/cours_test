package casa.util.dodwanclient;

import casa.util.pdu.Pdu;
import java.net.SocketException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
* JUnit tests for the {@link Session} class
*/
public class SessionTest
{
    private static final int CONN_OPENED = 0;
    private static final int CONN_CLOSED = 1;
    private static final int CONN_ERR = 2;
    private static final int SESS_STING = 3;
    private static final int SESS_STED = 4;
    private static final int SESS_STOP = 5;

    private final Pdu okPdu, errPdu;

    private Session instance;
    private ConnectionMock connection;
    private SessionListenerMock listener;

    public SessionTest()
    {
        okPdu = new TestPdu();
        okPdu.putString("name", "ok");
        errPdu = new TestPdu();
        errPdu.putString("name", "error");
    }

    /**
    * Check the succession of events when invoking the {@link Session#start()} method
    */
    @Test
    public void testStart() throws Exception
    {
        // The start() method should succeed
        int[] expectedOK = { SESS_STING, CONN_OPENED, SESS_STED };
        String[] expectedWrittenOK = { "hello", "start" };
        start(expectedOK, expectedWrittenOK, 1, okPdu, 0);

        // The connection cannot be opened
        int[] expectedNoConn = { SESS_STING, CONN_ERR, CONN_ERR, CONN_ERR, SESS_STOP };
        start(expectedNoConn, null, 0, okPdu, 0);

        // The connection succeeds at the 3rd attempt and the session is successfully started
        int[] expectedConnErr = { SESS_STING, CONN_ERR, CONN_ERR, CONN_OPENED, SESS_STED };
        start(expectedConnErr, expectedWrittenOK, 3, okPdu, 0);

        // The connection succeeds, but the hello PDUs are not acknoledged
        int[] expectedNoAck = { SESS_STING, CONN_OPENED, CONN_CLOSED };
        String[] expectedWrittenNoAck = { "hello", "hello", "hello" };
        start(expectedNoAck, expectedWrittenNoAck, 1, errPdu, 0);

        // A stop command occurs while starting
        int[] expectedStart = { SESS_STING, CONN_OPENED };
        int[] expectedStop = { SESS_STOP, CONN_CLOSED };
        start(expectedStart, null, 1, okPdu, 10);
        setListener(expectedStop);
        instance.stop();
        listener.checkEvents();
    }

    /**
    * Check the events when an error occurs while the session is started
    */
    @Test
    public void testError() throws Exception
    {
        // Disconnect and then restart
        int[] expectedStart = { SESS_STING, CONN_OPENED, SESS_STED };
        int[] expectedDis = { CONN_CLOSED, SESS_STING, CONN_OPENED, SESS_STED };
        start(expectedStart, null, 1, okPdu, 0);
        setListener(expectedDis);
        connection.disconnect();
        listener.checkEvents();

        // Connection error and the connection cannot be opened
        int[] expectedErr = { CONN_ERR, CONN_CLOSED, SESS_STING, CONN_ERR, CONN_ERR, CONN_ERR, SESS_STOP };
        start(expectedStart, null, 1, okPdu, 0);
        setListener(expectedErr);
        connection.setAcceptConnect(false);
        connection.onError(new SocketException(""));
        listener.checkEvents();

        // Unknown error
        int[] expectedUnknownErr = { CONN_ERR, CONN_CLOSED, SESS_STING, CONN_ERR, CONN_ERR, CONN_ERR, SESS_STOP };
        start(expectedStart, null, 1, okPdu, 0);
        setListener(expectedErr);
        connection.setAcceptConnect(false);
        connection.onError(new SocketException(""));
        listener.checkEvents();
    }

    /**
    * Check the events when the session is started and then stopped
    */
    @Test
    public void testStop() throws Exception
    {
        // the stop() should succeed
        int[] expectedStart = { SESS_STING, CONN_OPENED, SESS_STED };
        int[] expectedStop = { SESS_STOP, CONN_CLOSED };
        start(expectedStart, null, 1, okPdu, 0);
        setListener(expectedStop);
        instance.stop();
        listener.checkEvents();

        // The "stop" PDU is not acknoledged
        start(expectedStart, null, 1, okPdu, 0);
        try {
            connection.setReply(errPdu, 0);
            setListener(expectedStop);
            instance.stop();
            fail("UnexpectedPduException should have occured");
        } catch (UnexpectedPduException e) { }
        listener.checkEvents();
    }

    /**
    * Start the session with some given configuration for the {@link ConnectionMock} instance
    * and the expected events
    */
    private void start(int[] expected, String[] expectedWritten, int connectAttempt, Pdu reply, int replyDelay) throws Exception
    {
        connection = new ConnectionMock(expectedWritten);
        if (connectAttempt > 1) {
            connection.setAcceptConnectAttempt(connectAttempt);
        } else {
            connection.setAcceptConnect(connectAttempt == 1);
        }
        connection.setReply(reply, replyDelay);

        instance = new SessionForTest(connection);
        setListener(expected);

        instance.start();
        listener.checkEvents();
        if (expectedWritten != null) {
            connection.checkWritten();
        }
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

/**
* A session class with a "fake" {@link Connection} instance
*/
class SessionForTest extends Session
{
    public SessionForTest(Connection connection)
    {
        super("clientId", connection, 3, 100, 500, 100);
    }
}
