package spanning_tree;

import csp_V1.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

public class NetworkSize {


    public static final String NETWORK_SIZE_TAG = "network_size";
    
    /**
     * Processus associé au neoud
     */
    ConcurrentProcess process;

    /**
     * Arbre recouvrant
     */
    SpanningTree spanning_tree;

    /**
     * Nombre de noeufs total de l'arbre (sert seulement pour la racine).
     */
    private int nbTot;

    /**
     * Nombre de fils du noeud.
     */
    private int nbSuccs;

    CountDownLatch blck;

    public NetworkSize(int id, String filename, int basePort) {

        this.process = new ConcurrentProcess(id, "node",basePort);
        this.process.setTrace(true);
        this.process.readNeighbouring(filename);
        this.process.startLoop();
        spanning_tree = new SpanningTree(this.process);

        // construction de l'arbre recouvrant
        this.spanning_tree.make();

        this.process.trace("NOEUD = " + this.spanning_tree.toString());

        this.nbSuccs = this.spanning_tree.getSuccessorCount();
        this.blck = new CountDownLatch(nbSuccs);
        this.nbTot = this.nbSuccs;

        if(spanning_tree.getFather() == -1) { // si le noeud courant est la racine
            this.nbTot += 1; // ses fils + lui même
        }
    }

    /**
     * Début le calcul d'énumération du réseau.
     */
    public void computeSize() {


        // Ajout du handler des messages permettant de calculer la taille du réseau
        this.process.addMessageListener(NetworkSize.NETWORK_SIZE_TAG, new MessageHandler() {

            @Override
            synchronized public void onMessage(Message msg) {

                blck.countDown(); // décrémente le nombre de fils qui doit m'envoyer leur sous total
                process.trace("NETWORKSIZE receive msg "+msg.getContent() + " from " + msg.getSourceId());
            

                int nbSuccsRcv = Integer.parseInt(msg.getContent());
                nbTot += nbSuccsRcv;

                if(spanning_tree.getFather() == -1) { // si le noeud courant est la racine alors fini et on affiche
                    process.printOut("NBTOT = " + nbTot);
                }             
            }
        });

        this.process.waitNeighbouring("Waiting for other process at NetworkSize:computeSize");
        
        try {
            this.blck.await();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
        
        if(this.spanning_tree.father.get() != -1) this.process.sendMessage(new Message(this.spanning_tree.father.get(), NetworkSize.NETWORK_SIZE_TAG, String.valueOf(this.nbTot))); // nb fils + moi

        this.process.exitLoop();
        this.process.printOut("finished");
    }

    public int getNbNode() {
        return this.nbTot;
    }

      
    /**
     * @param args args[0]= fichier de configuration du réseau
     *             args[1]= identité du noeud
     *             args[2]= base pour les ports d'E/S
     */
    public static void main(String[] args){

    String filename= args[0];
    int my_id= Integer.parseInt(args[1]); 
    int base_port= (args.length>2) ? Integer.parseInt(args[2]):0;
    NetworkSize ns = new NetworkSize(my_id, filename, base_port);
    ns.computeSize();
    //new Broadcast(my_id,filename,base_port+1000).broadcast("TOTAL NUMBER OF NODES = " + ns.getNbNode());
    }
}