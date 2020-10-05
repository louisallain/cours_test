/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 */

package build_net;

import java.security.InvalidParameterException;
import java.util.Set;
import java.util.concurrent.Semaphore;

import csp_V1.ConcurrentProcess;
import csp_V1.Message;
import csp_V1.MessageHandler;

/**
 * Test de la méthode {@link csp_V1.ConcurrentProcess#waitNeighbouring()}
 */
public class BuildNet {

  private static final String HELLO_TAG = "hello";
  protected static final String ACK_TAG = "ack";
  
  /**
   * Sémaphore pour être bloqué tant qu'on a pas reçu un accusé de réception 
   * de tous les voisins
   */
  Semaphore process_terminated;
  /**
   * Ensemble des voisins attendus
   */
  Set<Integer> awaited_neigbours;
  ConcurrentProcess process;
  
  /**
   * @param id
   */
  public BuildNet(int id, String filename) {
    
    process_terminated= new Semaphore(0);
    process= new ConcurrentProcess(id,"BuildNet", 12231);
    process.readNeighbouring(filename);
    awaited_neigbours= process.getNeighbourSet(); 
    process.printOut("running");
    process.setTrace(true);
    process.addMessageListener(HELLO_TAG, new MessageHandler() {
      
      @Override
      public void onMessage(Message msg) {
        //process.printOut("receive "+msg.getContent());
        if (awaited_neigbours.contains(msg.getSourceId())){
          process.sendMessage(new Message(msg.getSourceId(),ACK_TAG, "ack from "+process.getMyId()));
          awaited_neigbours.remove(msg.getSourceId());
          if (awaited_neigbours.isEmpty()){
            process.printOut("received a message from all neighbours");
            process_terminated.release();
           }
        }else{
          process.printOut("unattended message from process "+msg.getSourceId());
        }
      }
    });
    process.addMessageListener(ACK_TAG, new MessageHandler() {
      
      @Override
      public void onMessage(Message msg) {
        //process.printOut("receive "+msg.getContent());
      }
    });
    process.startLoop();

    for(int pid:process.getNeighbourSet()){
      try {
        process.printOut("sending hello to "+pid);
        process.sendMessage(new Message(pid, HELLO_TAG, "hello from "+process.getMyId()));
      } catch (InvalidParameterException e) {
        process.printErr(e.getMessage());
      }
    }
    try {
      process_terminated.acquire();
    } catch (InterruptedException ignore) {
    }
    //process.printOut(process.getRcvMsgCnt()+" sync messages received, "+process.getSndMsgCnt()+" sync messages sent");

    process.exitLoop();
    process.printOut(process.getRcvMsgCnt()+" messages received, "+process.getSndMsgCnt()+" messages sent");
    process.printOut("exiting");    
  }

  /**
   * @param args args[0]: fichier de configuration du réseau
   *             args[1]: identité du processus
   */
  public static void main(String[] args){
    
    String conf_file= args[0];
    int my_id= Integer.parseInt(args[1]);
    BuildNet buildNet= new BuildNet(my_id,conf_file);
  }

}
