/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 * @date janvier 2015
 */

package spanning_tree;

import java.util.concurrent.Semaphore;

import csp_V1.ConcurrentProcess;
import csp_V1.Message;
import csp_V1.MessageHandler;

/**
 * Programme de test de l'algorithme de parcours parallèle
 * par diffusion d'un message par le noeud racine
 * (après construction de l'arbre recouvrant)
 */
public class Broadcast{

  /**
   * Tag utilisé pour la diffusion d'un message par le noeud racine
   */
  public static final String BROADCAST_TAG= "broadcast";
  
  /**
   * identifiant du noeud
   */
  private int id;
  /**
   * Processus associé au neoud
   */
  ConcurrentProcess process;
  /**
   * Arbre recouvrant
   */
  SpanningTree spanning_tree;
  /** 
   * Sémaphore pour terminer seulement après avoir diffusé le message
   */
  Semaphore message_broadcasted;
  
  /**
   * Initialisation du processus, 
   * puis construction de l'arbre recouvrant
   * @param id numéro du processus
   * @param filename fichier de configuration du réseau
   * @param base_port adresse de base pour les ports d'E/S des noeuds
   */
  public Broadcast(int id, String filename, int base_port) {
    this.id= id;
    process= new ConcurrentProcess(id, "node",base_port);
    process.setTrace(true);
    process.readNeighbouring(filename);
    process.startLoop();
    spanning_tree= new SpanningTree(process);
    message_broadcasted= new Semaphore(0);
    process.addMessageListener(BROADCAST_TAG, new MessageHandler() {
      
      @Override
      public void onMessage(Message msg) {
        process.printOut("receive msg "+msg.getContent() + " from " + msg.getSourceId());
        for (int succ:spanning_tree.getSuccessors()){
          process.sendMessage(new Message(succ, BROADCAST_TAG, "hello "));
        }
        message_broadcasted.release();// on peut terminer maintenant
      }
    });
    // construction de l'arbre recouvrant
    spanning_tree.make();
    process.printOut("msg sent= "+process.getSndMsgCnt()+
                   ", msg received= "+process.getRcvMsgCnt());
  }
  
  /**
   * Diffusion d'un message de HELLO par le noeud racine
   * @see java.lang.Thread#run()
   */
  public void broadcast(String content){
    if (process.getMyId()==0){ // diffusion d'un message à tous les noeuds
      for (int succ:spanning_tree.getSuccessors()){
        process.sendMessage(new Message(succ, BROADCAST_TAG, content));
        process.exitLoop();
        process.printOut("finished");
      }
    }else{ // attendre d'avoir reçu le message et l'avoir diffusé
      try {
        message_broadcasted.acquire();  
      } catch (InterruptedException ignore) {
      }
      process.exitLoop();
      process.printOut("finished");
    }
  }
  
  /**
   * Lancement de diffusion d'un message vers tous les noeuds
   * @param args args[0]= fichier de configuration du réseau
   *             args[1]= identité du noeud
   *             args[2]= base pour les ports d'E/S
   */
  public static void main(String[] args){
    
    String filename= args[0];
    int my_id= Integer.parseInt(args[1]); 
    int base_port= (args.length>2) ? Integer.parseInt(args[2]):0;
    new Broadcast(my_id,filename,base_port).broadcast("hello from root");
  }

}
