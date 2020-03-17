package casa.util.dodwanclient;

import casa.util.Future;
import casa.util.Processor;
import casa.util.pdu.BsonPduSerializer;
import casa.util.pdu.Pdu;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class PduChannelTest
{
    private static final int CONN_OPENED = 0;
    private static final int CONN_CLOSED = 1;
    private static final int CONN_ERR = 2;

    private static int TEST_PORT = 23456;

    private PduChannel instance;
    private SessionListenerMock listener;
    private Connection connection;

    @Test
    public void testWrite() throws Exception
    {
        String[] expectedRead = { "hello", "start", "stop" };
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer(), expectedRead)) {
            open(console);
            int cpt = 0;
            for (String name : expectedRead) {
                instance.write(makePdu(name, "w" + cpt++));
            }
            Thread.sleep(10);
            console.checkRead();
        }
    }

    @Test
    public void testWriteForReply() throws Exception
    {
        String expectedReply = "ok";
        String[] expectedRead = { "hello", "start" };
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer(), expectedRead)) {
            open(console);
            int cpt = 0;
            for (String name : expectedRead) {
                this.writeAsync(console, makePdu(expectedReply, "w" + cpt), 50);
                Pdu reply = instance.writeForReply(makePdu(name, "w" + cpt++), 100);
                assertNotNull(reply);
                assertEquals(expectedReply, reply.getString("name"));
            }
            Thread.sleep(10);
            console.checkRead();
        }
    }

    @Test
    public void testWriteForReplyAndCheck() throws Exception
    {
        String expectedReply = "ok", unexpectedReply = "error";
        String[] expectedRead = { "hello", "start" };
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer(), expectedRead)) {
            open(console);
            int cpt = 0;
            for (String name : expectedRead) {
                this.writeAsync(console, makePdu(expectedReply, "w" + cpt), 50);
                Pdu reply = instance.writeForReply(makePdu(name, "w" + cpt++), 100, expectedReply);
                assertNotNull(reply);
                assertEquals(expectedReply, reply.getString("name"));
            }
            Thread.sleep(10);
            console.checkRead();
        }

        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer(), expectedRead)) {
            open(console);
            int cpt = 0;
            for (String name : expectedRead) {
                this.writeAsync(console, makePdu(unexpectedReply, "w" + cpt), 50);
                try {
                    Pdu reply = instance.writeForReply(makePdu(name, "w" + cpt++), 100, expectedReply);
                    fail("Unexpected reply should fail");
                } catch (UnexpectedPduException e) { }
            }
            Thread.sleep(10);
            console.checkRead();
        }
    }

    @Test
    public void testProcessor() throws Exception
    {
        String expected = "test", unexpected = "notest";
        try (ConsoleSocket console = new ConsoleSocket(TEST_PORT+1, new BsonPduSerializer())) {
            open(console);
            Future<Boolean> lock = new Future<>(1000, false);
            instance.putProcessor(expected, new Processor<Pdu>() {
                @Override
                public void process(Pdu pdu) {
                    assertEquals(expected, pdu.getString("name"));
                    lock.putValue(true);
                }
            });
            console.write(makePdu(unexpected, null));
            console.write(makePdu(expected, null));
            assertTrue("Processor not fired", lock.getValue());
        }
    }

    private void writeAsync(ConsoleSocket console, Pdu pdu, int delay)
    {
        new Thread(){
            public void run() {
                try {
                    Thread.sleep(delay);
                    console.write(pdu);
                } catch (Exception e) { }
            }
        }.start();
    }

    private void open(ConsoleSocket console) throws Exception
    {
        console.start();
        Thread.sleep(100);

        connection = new Connection("localhost", console.getPort(), new BsonPduSerializer(), 500);
        instance = connection.getChannel();

        int[] expected = { CONN_OPENED };
        setListener(expected);
        connection.connect();
        listener.checkEvents();

        Thread.sleep(10);
        assertTrue(console.isConnected());
        instance.open();
    }

    private void setListener(int[] expected)
    {
        if (listener != null) {
            connection.removeListener(listener);
        }
        listener = new SessionListenerMock(expected);
        connection.addListener(listener);
    }

    private Pdu makePdu(String name, String token)
    {
        Pdu pdu = new TestPdu();
        pdu.putString("name", name);
        if (token != null) {
            pdu.putString("tkn", token);
        }
        return pdu;
    }
}
