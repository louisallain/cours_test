package csp_V1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
    CountDownLatch neighbour_awaited;
    ConcurrentHashMap<Integer, SyncState> sync_state_table;

    public ConcurrentProcess(int id, String name, int offset) {
        
        super(name);
        this.my_id = id;
        this.name = name;
        this.listener_map = new ConcurrentHashMap<>();
        this.neighbouring = new Neighbouring(this, offset);
        this.rcv_msg_cnt = new AtomicInteger(0);
        this.snd_msg_cnt = new AtomicInteger(0);
        this.ready = new AtomicBoolean(false);
        this.sync_state_table = new ConcurrentHashMap<>();
        this.addMessageListener(SyncState.SYNC_TAG, new SyncMessageHandler(this));
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
            this.trace("Neighbouring read");
        } catch(IOException ioe) {
            this.printErr("[ConcurrentProcess : readNeighbouring] Error while reading neighbouring file.");
            ioe.printStackTrace();
        }

        // Initialise la table des synchros
        for (int id : this.getNeighbourSet()) {
            this.sync_state_table.put(id, SyncState.NOTHING_SND_NOTHING_RCV); 
        }

        // Initialise nombre de voisins en attente, init = tous les voisins
        this.neighbour_awaited = new CountDownLatch(this.sync_state_table.size()); 
        this.trace("Nombre de voisins en attente : " + this.neighbour_awaited.getCount());
    }

    void resetNeighbouringStateCounter() {

        if(this.neighbour_awaited == null || this.neighbour_awaited.getCount() == 0) {
            this.neighbour_awaited = new CountDownLatch(this.neighbouring.size());
        }
    }

    public final void waitNeighbouring(String msg) {
        
        this.printOut(msg);
        this.waitNeighbouring();        
    }

    public final void waitNeighbouring() {

        synchronized(this.sync_state_table) {

            this.resetNeighbouringStateCounter();

            for(Integer id : this.neighbouring.getIdentities()) {

                SyncState state = this.sync_state_table.get(id);

                if(state == null) {
                    state = SyncState.NOTHING_SND_NOTHING_RCV;
                    this.sync_state_table.put(id, state);
                }

                this.trace("Processus / etat : " + id + " / " + state);
                this.trace("Taille  : " + this.sync_state_table.size());

                if(state == SyncState.NOTHING_SND_NOTHING_RCV) {
                    this.sendMessage(new Message(id, SyncState.SYNC_TAG, SyncState.SYNC_MSG_READY));
                    this.trace("Ready sent to process num " + id);
                    this.sync_state_table.put(id, SyncState.READY_SND_NOTHING_RCV);
                }  
                else if(state == SyncState.NOTHING_SND_READY_RCV) {
                    this.sendMessage(new Message(id, SyncState.SYNC_TAG, SyncState.SYNC_MSG_ACK));
                    this.trace("ACK sent to process num " + id);
                    this.sync_state_table.put(id, SyncState.ACK_SND_READY_RCV);
                }
            }
        }

        try {
            this.neighbour_awaited.await();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /*
    public final void waitNeighbouring() {
        
        this.sync_state_table.forEach((k, v) -> {

            if(v == SyncState.NOTHING_SND_NOTHING_RCV || v == SyncState.NOTHING_SND_READY_RCV) {

                Message msg = new Message(k, SyncState.SYNC_TAG, SyncState.SYNC_MSG_READY);
                msg.setSourceId(this.getMyId());

                // On renvoie le message près tant que l'état du voisin n'a pas changé. 
                // i.e. tant qu'on a pas reçu l'ack dans la méthode SyncMessageHandler.onMessage
                this.printOut("Processus / etat : " + k + " / " + this.sync_state_table.get(k));
                this.printOut("Taille  : " + this.sync_state_table.size());
                while(this.sync_state_table.get(k) != SyncState.ACK_SND_READY_RCV) {
                    
                    this.sendMessage(msg);
                }

                // Met à jour l'état du processus :
                this.sync_state_table.put(k, SyncState.READY_SND_NOTHING_RCV);
                this.trace("Ready sent to process num " + k);
            }
        });

        try {
            this.neighbour_awaited.await();
            this.ready.set(true);            
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    */

    public final Set<Integer> getNeighbourSet() {
        return this.neighbouring.getIdentities();
    }

    public final int getNeighbourCount() {
        return this.neighbouring.size();
    }

    public final void sendMessage(Message msg) {
        
        msg.setSourceId(this.getMyId());
        int dest_id = msg.getDestinationId();

        try {
            byte[] sendBuf = msg.toBytes();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, this.neighbouring.getOutputAddress(dest_id), this.neighbouring.getOutputPort(dest_id));
            int bytesCount = packet.getLength();
            this.neighbouring.getOutputSocket(dest_id).send(packet);
            this.snd_msg_cnt.getAndIncrement();
            //this.trace("Message sent to : " + this.neighbouring.getOutputAddress(dest_id).toString() + ":" + this.neighbouring.getOutputPort(dest_id));
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