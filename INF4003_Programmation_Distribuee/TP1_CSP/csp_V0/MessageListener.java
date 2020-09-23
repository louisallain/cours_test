package csp_V0;

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

        byte[] buffer = new byte[Message.CONTENT_MAX_SIZE];
        DatagramPacket udpData = new DatagramPacket(buffer, Message.CONTENT_MAX_SIZE);
        try {
            this.socket.receive(udpData);
            Message msg = Message.fromBytes(udpData.getData(), udpData.getLength());
            this.process.receiveMessage(msg);
        } 
        catch(SocketTimeoutException ste) {
            this.process.printErr("[MessageListener : inLoop] Error timeout.");
        }
        catch(PortUnreachableException pue) {
            this.process.printErr("[MessageListener : inLoop] Error port unreachable.");
        }
        catch(IllegalBlockingModeException ibme) {
            this.process.printErr("[MessageListener : inLoop] Error illegal blocking mode.");
        }
        catch(ClassNotFoundException cne) {
            this.process.printErr("[MessageListener : inLoop] Error class not found.");
        }
        catch(IOException ioe) {
            this.process.printErr("[MessageListener : inLoop] Error IO.");
        }
    }

    public void beforeLoop() {
        // Rien à faire
    }

    public void afterLoop() {
        this.socket.close();
    }
}