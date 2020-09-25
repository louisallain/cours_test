/**
 * Programme de test de la bibliothèque de communication évènementielle
 * @author F. Raimbault
 * @date septembre 2019
 */
package ping_pong_V1;

import java.util.concurrent.Semaphore;

import csp_V1.ConcurrentProcess;
import csp_V1.Message;
import csp_V1.MessageHandler;

/**
 * Classe contenant le code : 
 * - d'un processus envoyant N messages au processus et 
 *   attendant un aquitemment avant chaque nouvel envoi
 * - d'un processus attendant N messages du processus et 
 *   renvoyant un aquitement pour chacun d'eux.
 */
public class PingPong{

  public static final String PING_TAG = "Ping";
  public static final String PONG_TAG = "Pong";
  
  int N; // nombre de messages à envoyer par Ping (et à acquiter par Pong)
  int current_msg_nb; // numéro du message courant
  ConcurrentProcess process;
  Semaphore ping_received; // utilisé par le processus Pong pour attendre un message du processus Ping
  Semaphore pong_received; // utilisé par le processus Ping pour attendre un message du processus Pong
  
  public PingPong(String name, int id, String filename, int round_trip){
    N= round_trip;
    current_msg_nb= 0;
    process= new ConcurrentProcess(id, name,11111);
    process.setTrace(true);
    process.readNeighbouring(filename);
  }
  
  public void pong(){
    ping_received= new Semaphore(0);
    process.addMessageListener(PING_TAG,new MessageHandler() {

      @Override
      synchronized public void onMessage(Message msg) {
        process.printOut("receive message "+msg);
        Message reply_msg= new Message(0, PONG_TAG, "ack"+current_msg_nb+" from "+process.getMyId());
        process.sendMessage(reply_msg);
        process.printOut("reply "+reply_msg); 
        ping_received.release();
      }
    });
    process.startLoop();
    do{
      try {
        ping_received.acquire(); // attend un ping
        current_msg_nb += 1;
      } catch (InterruptedException e) {
        break;
      }
    }while (current_msg_nb != 10);
    process.printOut(process.getSndMsgCnt()+" messages sent");
    process.printOut(process.getRcvMsgCnt()+" messages received");
    process.exitLoop();
 }

  public void ping() {

    pong_received= new Semaphore(0);
    process.addMessageListener(PONG_TAG, new MessageHandler() {

      @Override
      synchronized public void onMessage(Message msg) {
        process.printOut("received message "+msg);
        pong_received.release();
      }
    });
    process.startLoop();
    do{
      Message msg= new Message(1, PING_TAG, "hello"+current_msg_nb+" from "+process.getMyId());
      process.printOut("send message "+msg); 
      process.sendMessage(msg); // envoie le ping
      try {
        pong_received.acquire(); // attend le pong
      } catch (InterruptedException e) {
        break;
      }
      current_msg_nb += 1;
    }while (current_msg_nb != 10);
    process.printOut(process.getSndMsgCnt()+" messages sent");
    process.printOut(process.getRcvMsgCnt()+" messages received");
    process.exitLoop();  
  }

  /**
   * 
   * @param args
   * args[0]= fichier de configuration du réseau
   * args[1]= numéro de processus, 0=Ping, 1=Pong
   * args[2]= nombre d'aller-retour
   */
  public static void main(String[] args){
    
    try {
      String filename= args[0]; // fichier de configuration du réseau
      int my_id= Integer.parseInt(args[1]); 
      int round_trips= Integer.parseInt(args[2]); 
      // Teste le second agument pour savoir s'il s'agit d'un processus Ping (0) ou d'un processus Pong (1)
      if (my_id == 0){
        new PingPong("ping",0,filename,round_trips).ping();
      }else{ 
        new PingPong("pong",1,filename,round_trips).pong();
      }
    } catch (NumberFormatException e) {
      System.err.println("incorrect parameter(s): "+e);
      System.err.println(e+"\nusage: PingPong config_filename process_no");
    } catch (ArrayIndexOutOfBoundsException e){
      System.err.println("incorrect parameter(s): "+e);
      System.err.println("\nusage: PingPong config_filename process_no");
    }
  }

}
