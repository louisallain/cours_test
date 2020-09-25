package csp_V1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

public final class MessageListener extends ThreadLoop {

    private DatagramSocket socket;
    private ConcurrentProcess process;

    MessageListener(ConcurrentProcess p, DatagramSocket s) {

        super("Message Listener");
        this.process = p;
        this.socket = s;
    }

    public void inLoop() {

        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        int bytesLong = packet.getLength();

        try {

            this.socket.receive(packet);
            Message msg = Message.fromBytes(buffer, bytesLong);
            this.process.receiveMessage(msg);
        } 
        catch(SocketTimeoutException ste) {
            //this.process.printErr("[MessageListener : inLoop] Error timeout.");
        }
        catch(PortUnreachableException pue) {
            pue.printStackTrace();
        }
        catch(IllegalBlockingModeException ibme) {
            ibme.printStackTrace();
        }
        catch(ClassNotFoundException cne) {
            cne.printStackTrace();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void beforeLoop() {
        // Rien à faire
    }

    public void afterLoop() {
        this.socket.close();
    }
}