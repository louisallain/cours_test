package spanning_tree;

import csp_V1.*;
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
     * Nombre de noeufs total du sous-arbre dont this.process est la racine ou tout l'arbre si this.process est la racine.
     */
    AtomicInteger nbTot;

    /**
     * Nombre de fils du noeud.
     */
    int nbSuccs;

    public NetworkSize(String filename, int basePort) {

        this.process = new ConcurrentProcess(id, "node",base_port);
        this.process.setTrace(true);
        this.process.readNeighbouring(filename);
        this.process.startLoop();
        spanning_tree = new SpanningTree(this.process);
        this.nbTot = new AtomicInteger(0);

        // construction de l'arbre recouvrant
        this.spanning_tree.make();

        this.nbSuccs = this.spanning_tree.successors.size();

        // Ajout du handler des messages permettant de calculer la taille du réseau
        this.process.addMessageListener(NetworkSize.NETWORK_SIZE_TAG, new MessageHandler() {
      
            @Override
            public void onMessage(Message msg) {

                process.printOut("receive msg "+msg.getContent() + " from " + msg.getSourceId());

                nbTot.set(nbTot.get() + nbSuccs);
                if(spanning_tree.getFather() == -1) { // si le noeud est la racine
                    process.printOut("Total number of nodes = " + nbTot);
                }   
                else {
                    process.sendMessage(new Message(spanning_tree.father, NetworkSize.NETWORK_SIZE_TAG, nbTot.get()));
                }
            }
        });
    }

    /**
     * Début le calcul d'énumération du réseau.
     */
    synchronized public void computeSize() {

        this.process.sendMessage(new Message(this.spanning_tree.father, NetworkSize.NETWORK_SIZE_TAG, this.nbSuccs));
        this.process.exitLoop();
        this.process.printOut("finished");
    }
}