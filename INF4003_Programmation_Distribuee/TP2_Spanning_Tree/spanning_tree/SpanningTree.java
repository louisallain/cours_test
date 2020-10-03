package spanning_tree;

import csp_V1.*;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class SpanningTree {

    /**
     * Le processus associé à cet arbre recouvrant
     */
    private ConcurrentProcess process;
    /**
     * Booléen indiquant si le site a été marqué
     */
    private boolean marked;
    /**
     * Ensemble des identités des successeurs
     */
    private Set<Integer> successors;
    /**
     * Identité du père du processus
     */
    private int father;
    /**
     * Nombre de messages acquittés avant d'envoyer son dernier acquitter.
     */
    private int nbAck;
    /**
     * Racine de l'arbre couvrant
     */
    private int root;
    /**
     * Entier indiquant si l'algorithme d'arbre recouvrant a été lancé (méthode make()).
     */
    CountDownLatch makeDoneCountDown;

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
        this.successors = new HashSet<Integer>();

        // Initialisation de l'algo de l'arbre couvrant
        this.marked = false;
        // Ensemble des voisins = this.process.getNeighbourSet()
        this.nbAck = 0;

        // Ajout des handlers des messages
        this.process.addMessageListener(SpanningTree.SPANNING_TAG, new SpanningMessageHandler(this.process, this));
    }

    public Set<Integer> getSuccessors() {

        return this.successors;
    }

    void setSuccessors(Set<Integer> successors) {
        this.successors = successors;
    }

    public int getSuccessorCount() {

        return this.successors.size();
    } 

    int getNbAck() {
        return this.nbAck;
    }

    void setNbAck(int nbAck) {
        this.nbAck = nbAck;
    }

    public int getFather() {
        
        return this.father;
    }

    void setFather(int father) {
        this.father = father;
    }

    boolean isMarked() {
        return this.marked;
    }

    void setMarked(boolean marked) {
        this.marked = marked;
    }

    int getRoot() {
        return this.root;
    }

    private void setRoot(int root) {
        this.root = root;
    }

    public void make() {

        this.process.waitNeighbouring("Waiting for other process...");
        
        this.makeDoneCountDown = new CountDownLatch(1);

        // Lors de la décision de lancer un parcours :
        this.setRoot(this.process.getMyId());
        this.marked = true;
        this.successors.addAll(this.process.getNeighbourSet());
        this.nbAck = this.successors.size();
        for(Integer id : (Set<Integer>)this.process.getNeighbourSet()) {
            this.process.sendMessage(new Message(id, SpanningTree.SPANNING_TAG, SpanningTree.TRAVERSER));
        }
    }

    public String toString() {

        String ret = "root = " + this.getRoot() + "father = " + this.getFather() + " childs = {";
        for(Integer id : this.successors) {
            ret += id + ",";
        }
        ret += "}";

        return ret;
    }

    public static void main(String[] args){
    
        String filename= args[0];
        int my_id= Integer.parseInt(args[1]); 
        int base_port= (args.length>2) ? Integer.parseInt(args[2]):0;
        
        ConcurrentProcess process = new ConcurrentProcess(my_id, "node", base_port);
        process.setTrace(true);
        process.readNeighbouring(filename);
        process.startLoop();
        SpanningTree spanning_tree = new SpanningTree(process);
        spanning_tree.make();
        process.printOut("spanning tree: "+spanning_tree.toString());
        process.printOut("msg sent= "+process.getSndMsgCnt()+
                    ", msg received= "+process.getRcvMsgCnt());
        process.exitLoop();
        process.printOut("finished");
      }
}