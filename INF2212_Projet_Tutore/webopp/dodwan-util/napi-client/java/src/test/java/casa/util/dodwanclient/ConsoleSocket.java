package casa.util.dodwanclient;

import casa.util.pdu.PduSerializer;
import casa.util.pdu.Pdu;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
* A console that allows to read/write PDUs to TCP clients
*/
public class ConsoleSocket extends Thread implements AutoCloseable
{
    private final PduSerializer serializer;
    private final int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private boolean connected, running;
    private String[] expectedRead;
    private int current, nbRead;

    public ConsoleSocket(int port, PduSerializer serializer) throws Exception
    {
        this.port = port;
        this.serializer = serializer;
    }

    public ConsoleSocket(int port, PduSerializer serializer, String[] expectedRead) throws Exception
    {
        this(port, serializer);
        this.expectedRead = expectedRead;
    }

    public void checkRead()
    {
        assertEquals("Number of read PDUs incorrect", expectedRead.length, nbRead);
    }

    public int getPort()
    {
        return this.port;
    }

    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public void run()
    {
        running = true;
        while (running) {
            try {
                this.serverSocket = new ServerSocket(port);
                this.serverSocket.setSoTimeout(1000);
                socket = serverSocket.accept();
                connected = true;
                socket.setSoTimeout(1000);
                while (connected) {
                    try {
                        Pdu pdu = new TestPdu();
                        int size = serializer.read(pdu, socket.getInputStream());
                        if (size < 0) {
                            throw new IOException("No PDU read from InputStream");
                        }
                        if (expectedRead != null) {
                            assertEquals("Unexpected pdu read", expectedRead[current++], pdu.getString("name"));
                            nbRead++;
                        }
                    } catch (SocketTimeoutException e) {
                        // ignore
                    } catch (Exception e) {
                        connected = false;
                    }
                }
            } catch (Exception e) {
                running = false;
            }
        }
    }

    @Override
    public void close()
    {
        running = false;
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (Exception e) { }
        }
    }

    public void write(Pdu pdu) throws Exception
    {
        if (!connected) {
            throw new SocketException("Not connected");
        }
        serializer.write(pdu, socket.getOutputStream());
    }
}
