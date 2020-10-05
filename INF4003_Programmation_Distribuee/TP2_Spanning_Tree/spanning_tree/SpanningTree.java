package spanning_tree;

import csp_V1.*;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

public class SpanningTree {

    /**
     * Le processus associé à cet arbre recouvrant
     */
    private ConcurrentProcess process;
    /**
     * Booléen indiquant si le site a été marqué
     */
    AtomicBoolean marked;
    /**
     * Ensemble des identités des successeurs
     */
    Set<Integer> successors;
    /**
     * Identité du père du processus
     */
    AtomicInteger father;
    /**
     * Nombre de messages acquittés avant d'envoyer son dernier acquitter.
     */
    AtomicInteger nbAck;

    /**
     * Tag d'un message SpanningTree
     */
    public static final String SPANNING_TAG = "spanning";

    /**
     * Contenu d'un message (acquitter, déjà vu)
     */
    public static final String ACK_DEJA_VU = "ack_deja_vu";

    /**
     * Contenu d'un message (acquitter, deja vu)
     */
    public static final String ACK_TERMINE = "ack_termine";

    /**
     * Contenu d'un message (traverser)
     */
    public static final String TRAVERSER = "traverser";

    public SpanningTree(ConcurrentProcess process) {

        this.process = process;

        // Initialisation de l'algo de l'arbre couvrant
        this.marked = new AtomicBoolean(false);
        this.nbAck = new AtomicInteger(0);
        this.father = new AtomicInteger(-1);
        this.successors = new HashSet<>();

        // Ajout des handlers des messages
        this.process.addMessageListener(SpanningTree.SPANNING_TAG, new SpanningMessageHandler(this.process, this));
    }

    public Set<Integer> getSuccessors() {
        return this.successors;
    }

    public int getSuccessorCount() {
        return this.successors.size();
    }

    public int getFather() {
        return this.father.get();
    }

    public boolean getMarked() {
        return this.marked.get();
    }

    public void make() {

        this.process.waitNeighbouring("Waiting for other process at Spanningtree:make");
        
        if(this.process.getMyId() == 0) {

            // Lors de la décision de lancer un parcours :
            this.marked.set(true);
            synchronized(this.successors) {
                this.successors.addAll(this.process.getNeighbourSet());
            }
            this.nbAck.set(this.getSuccessorCount());
            for(Integer id : (Set<Integer>)this.process.getNeighbourSet()) {
                this.process.sendMessage(new Message(id, SpanningTree.SPANNING_TAG, SpanningTree.TRAVERSER));
            }
        }
    }

    public String toString() {

        String ret = "current node = " + this.process.getMyId() + " father = " + this.getFather() + " childs = {";
        synchronized(this.successors) {
            for(Integer id : this.getSuccessors()) {
                ret += id + ",";
            }
        }
        ret += "}";

        return ret;
    }
}