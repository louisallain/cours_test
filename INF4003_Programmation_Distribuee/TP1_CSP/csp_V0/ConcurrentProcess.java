package csp_V0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConcurrentProcess extends ThreadLoop {

    private ConcurrentHashMap<String, MessageHandler> listener_map;
    private int my_id;
    private String name;
    private Neighbouring neighbouring;
    private AtomicInteger rcv_msg_cnt;
    private AtomicBoolean ready;
    private AtomicInteger snd_msg_cnt;

    public ConcurrentProcess(int id, String name, int offset) {
        
        super(name);
        this.my_id = id;
        this.name = name;
        this.listener_map = new ConcurrentHashMap<>();
        this.neighbouring = new Neighbouring(this, offset);
        this.rcv_msg_cnt = new AtomicInteger(0);
        this.snd_msg_cnt = new AtomicInteger(0);
        this.ready = new AtomicBoolean(false);
    }

    public ConcurrentProcess(int id, String name) {

        this(id, name, 1);
    }

    public final int getMyId() {
        return this.my_id;
    }

    public final int getSndMsgCnt() {
        return this.snd_msg_cnt.get();
    }

    public final int getRcvMsgCnt() {
        return this.rcv_msg_cnt.get();
    }

    public final void resetSndMsgCnt() {
        this.snd_msg_cnt.set(0);
    }

    public final void resetRcvMsgCnt() {
        this.rcv_msg_cnt.set(0);
    }

    public boolean isReady() {
        return this.ready.get();
    }

    public final void addNeighbour(int local_port,
                               String remote_host_name,
                               int remote_id,
                               int remote_port) {
        
        try {
            this.neighbouring.add(local_port, remote_host_name, remote_id, remote_port);        
        } catch(Exception e) {
            this.printErr("[ConcurrentProcess : addNeighbour] " + e);
        }
    }

    public final void readNeighbouring(String filename) {

        try {
            this.neighbouring.read(filename);
        } catch(IOException ioe) {
            this.printErr("[ConcurrentProcess : readNeighbouring] Error while reading neighbouring file.");
        }
    }

    private final void waitNeighbouring(String msg) {
        
        try {
            this.printOut(msg);
            new BufferedReader(new InputStreamReader(System.in)).readLine();
            this.trace("unblocked");
            this.ready.set(true); 
        } catch(IOException ioe) {
            this.printErr("[ConcurrentProcess : waitNeighbouring)] Error");
        }
    }

    public final Set<Integer> getNeighbourSet() {
        return this.neighbouring.getIdentities();
    }

    public final int getNeighbourCount() {
        return this.neighbouring.size();
    }

    public final void sendMessage(Message msg) {
        
        msg.setSourceId(this.my_id);
        try {
            int dest_id = msg.getDestinationId();
            DatagramPacket dp = new DatagramPacket(msg.toBytes(), msg.getContent().length(), this.neighbouring.getOutputAddress(dest_id), this.neighbouring.getOutputPort(dest_id));
            this.neighbouring.getOutputSocket(dest_id).send(dp);
            this.printOut("Message sent : " + this.neighbouring.getOutputAddress(dest_id).toString());
        } catch(IOException ioe) {
            this.printErr("[ConcurrentProcess : sendMessage)] " + ioe);
        }
        
    }

    public final void addMessageListener(String tag, MessageHandler listener) {
        this.listener_map.put(tag, listener);
    } 
    
    void receiveMessage(Message msg) {
        this.listener_map.get(msg.getTag()).onMessage(msg);
    }

    void beforeLoop() {

        this.neighbouring.open();
        this.waitNeighbouring("ConcurrentProcess : beforeLoop] Waiting for synchronize.");
    }

    void inLoop() {

        try {
            ConcurrentProcess.sleep(1000);
        } catch(InterruptedException ie) {
            this.printErr("[ConcurrentProcess : inLoop)] " + ie);
        }
    }

    void afterLoop() {
        this.neighbouring.close();
    }
}