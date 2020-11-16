package csp_V2;

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
    private int nbNodes;

    public ConcurrentProcess(int id, String name, int offset, int nbNodes) {
        
        super(name);
        this.nbNodes = nbNodes;
        this.my_id = id;
        this.name = name;
        this.listener_map = new ConcurrentHashMap<>();
        this.neighbouring = new Neighbouring(this, offset);
        this.rcv_msg_cnt = new AtomicInteger(0);
        this.snd_msg_cnt = new AtomicInteger(0);
        this.ready = new AtomicBoolean(false);
    }

    public ConcurrentProcess(int id, String name, int nbNodes) {

        this(id, name, 1, nbNodes);
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
            ioe.printStackTrace();
        }
    }

    /**
     * Bloque le noeud courant tant que tous les autres noeuds ne sont pas arrivés à cette même méthode/
     * En utilisant Zookeeper, le principe est que chaque noeud entre dans la barrière en créant un noeud éphémère séquentiel sous /allain1/barriere par exemple.
     * Un noeud reste dans la barrière tant que tous les noeuds ne sont pas dans la barrière.
     * ie. si le nombre de fils sous /allain1/barriere est différent du nombre de noeuds total.
     * @param msg message à afficher avant la synchro
     */
    public final void waitNeighbouring(String msg) {
        this.printOut(msg);
        Barriere b = new Barriere(this.nbNodes);
        b.enter("Unblocked from waitNeighbouring");
        
    }

    public final Set<Integer> getNeighbourSet() {
        return this.neighbouring.getIdentities();
    }

    public final int getNeighbourCount() {
        return this.neighbouring.size();
    }

    public final void sendMessage(Message msg) {
        
        msg.setSourceId(this.my_id);
        int dest_id = msg.getDestinationId();

        try {

            byte[] sendBuf = msg.toBytes();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, this.neighbouring.getOutputAddress(dest_id), this.neighbouring.getOutputPort(dest_id));
            int bytesCount = packet.getLength();
            this.neighbouring.getOutputSocket(dest_id).send(packet);
            this.trace("Message sent to : " + this.neighbouring.getOutputAddress(dest_id).toString() + ":" + this.neighbouring.getOutputPort(dest_id));
            this.snd_msg_cnt.getAndIncrement();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        
    }

    public final void addMessageListener(String tag, MessageHandler listener) {
        this.listener_map.put(tag, listener);
    } 
    
    void receiveMessage(Message msg) {
        this.listener_map.get(msg.getTag()).onMessage(msg);
        this.rcv_msg_cnt.getAndIncrement();
    }

    void beforeLoop() {

        this.neighbouring.open();
        this.waitNeighbouring("[ConcurrentProcess : beforeLoop] Waiting for synchronize.");
    }

    void inLoop() {

        try {
            Thread.sleep(1000);
        } catch(InterruptedException ie) {
            this.printErr("[ConcurrentProcess : inLoop)] " + ie);
        }
    }

    void afterLoop() {
        this.neighbouring.close();
    }
}