package allain1.philosophers_V0;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Random;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

/**
 * Programme de simulation du problème des Philosophes avec un espace de tuples.
 * Utilise la solution de Dijkstra par sémaphores.
 * Convention utilisée : 
 * Le philosophe numéro i utilise la baguette numéro i à sa gauche et
 * la baguette numéro ((i+1)%nombre de philosophes) à sa droite
 */
public class Philosopher {

  /**
   * Nombre de cycles d'actions effectuées par chaque philosophe
   */
  private static final int NUMBER_OF_CYCLES = 10;
  /**
   * temps max (en ms) passé à l'action "penser"
   */
  private static final int MAX_THINKING_TIME = 1000;
  /**
   * temps max (en ms) passé à l'action "manger"
   */
  private static final int MAX_EATING_TIME = 500;
  /**
   * l'espace partagé
   */
  private JavaSpace space;
  /**
   * le numéro de ce philosophe
   */
  private int phi_id;
  /**
   * le nombre total de pholosophes
   */
  private int number_of_phi;
  /**
   * Temps maximal d'attente pour l'obtention de l'assiette et des couverts (en millisecondes)
   */
  private long max_waiting_time;
  /**
   * droit d'accès à l'assiette (avant de demander des baguettes)
   * Créé uniquement par le philosophe numéro 0
   */
  private SemaphoreAccessor plate;
  /**
   * Droit d'accès à la baguette gauche
   * (de numéro phi_id)
   * Créé uniquement par le philosophe numéro 0
    */
  private SemaphoreAccessor left;
  /**
   * droit d'accès à la baguette droite
   * (de numéro (phi_id+1) % number_of_phi)
   * Créé uniquement par le philosophe numéro 0
    */
  private SemaphoreAccessor right;
  /**
   * générateur de nombre aléatoire
   */
  private Random rnd;
  /**
   * prefix dans l'affichage sur la console, 
   * n caractères '\t' pour le philosophe n 
   */
  private String prefix="";
  
  /**
   * Création d'un philosophe. 
   * Le numéro 0 crée les sémaphores d'accès à l'assiette et aux baguettes 
   * et la barrière de synchronisation
   * @param space l'espace partagé
   * @param id le numéro du philosophe à table
   * @param num le nombre de philosophes à table
   */
  public Philosopher(JavaSpace space, int id, int num) {
    this.space = space;
    this.phi_id = id;
    this.number_of_phi = num;
    plate = new SemaphoreAccessor(space, "plate");
    left = new SemaphoreAccessor(space, "chopstick" + id);
    right = new SemaphoreAccessor(space, "chopstick" + ((id + 1) % num));
    
    if (id == 0) {
      initSpace();
    }
    max_waiting_time= 0;
    for(int i=0;i<id;i++){
      prefix+="\t"; // pour indenter avec i '\t' la sortie du philosophe i
    }
    rnd= new Random(System.currentTimeMillis());
  }

  /**
   * Création des sémaphores par le philosophe numéro 0 :
   * - 1 sémaphore d'accès aux assiettes (number_of_phi-1 accès autorisés)
   * - les sémaphores d'accès aux baguettes (1 seul accès autorisé)
   * + Création de la barrière de synchronisation pour la terminaison
   */
  private void initSpace() {
    plate.create(number_of_phi - 1);
    for (int i = 0; i < number_of_phi; i++) {
      SemaphoreAccessor chopstick = new SemaphoreAccessor(space, "chopstick" + i);
      chopstick.create(1);
    }
    Counter barrier= new Counter("barrier", number_of_phi);
    try {
      space.write(barrier, null, Lease.FOREVER);
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (TransactionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Suppression des sémaphores par le philosophe numéro 0 :
   * - le sémaphore d'accès aux assiettes (number_of_phi-1 accès autorisés)
   * - les sémaphores d'accès aux baguettes 
   * - le compteur partagé pour le round-robin
   */
  private void freeSpace() {
    say("releasing plates");
    SemaphoreAccessor plate = new SemaphoreAccessor(space, "plate");
    for (int i = 0; i < number_of_phi-1; i++) {
      plate.down();
    }
    say("releasing chopticks");
    for (int i = 0; i < number_of_phi; i++) {
      SemaphoreAccessor chopstick = new SemaphoreAccessor(space, "chopstick" + i);
      chopstick.down();
    }
    say("releasing round robin counter");
  }

  /**
   * Tâche d'un philosophe : number_of_tasks cycles des actions penser ; attendre ; manger ; libérer
   * Affiche le temps d'attente maximal en fin de tâche
   */
  public void working() {
    say("start working");
    for (int i = 0; i < NUMBER_OF_CYCLES; i++) {
      say("entering task loop "+i);
      thinking();
      waiting();
      eating();  
      releasing();
    }
    say("stop working (max waiting time="+max_waiting_time+").");
  }

  /**
   * Simule l'action d'attendre. 
   * Affiche le temps d'attente.
   * Mémorise le temps maximal d'attente
   */
  private void waiting(){
    say("start waiting for my turn");
    long time1= new Date().getTime();
    say("waiting for a plate");
    plate.down();
    say("waiting for the left chopstick");
    left.down();
    say("waiting for the right chopstick");
    right.down();
    long time2= new Date().getTime();
    long last= time2-time1;
    say("stop waiting. Has wait for " + last + " millisec.");
    if (last > max_waiting_time) max_waiting_time= last;
  }
  
  /**
   * Libère ses baguettes et son accès à l'assiette
   */
  private void releasing(){
    say("releasing chopsticks and plate");
    right.up();
    left.up();
    plate.up();
    say("stop releasing");
  }
  
  /**
   * Simule l'action de penser par une pause aléatoire entre 0 et MAX_THINKING_TIME sec. 
   */
  private void thinking() {
    int time = rnd.nextInt(MAX_THINKING_TIME);
    say("start thinking for " + time + " millisec.");
    try {
      Thread.sleep(time);
    } catch (Exception e) {
      e.printStackTrace();
    }
    say("stop thinking");
  }

  /**
   * Simule l'action de manger par une pause aléatoire entre 0 et MAX_EATING_TIME sec. 
   */
  private void eating() {
    int time = rnd.nextInt(MAX_EATING_TIME);
    say("start eating for " + time + " millisec.");
    try {
      Thread.sleep(time);
    } catch (Exception e) {
      e.printStackTrace();
    }
    say("stop eating");
  }

  /**
   * Barrière de synchonisation pour la suppression de tous les sémaphores 
   * par le philosophe numéro 0
   */
  private void cleaning(){
    say("start cleaning");
    try {
      Counter tmpl= new Counter("barrier");
      Counter barrier = (Counter) space.take(tmpl, null, Long.MAX_VALUE);
      barrier.decrement();
      space.write(barrier,null,Lease.FOREVER);
      if (phi_id==0){
        say("waiting for all the others");
        tmpl= new Counter("barrier",0);
        space.take(tmpl, null, Long.MAX_VALUE);
        freeSpace();
        say("all the philosophers have properly stopped");
//        say("liste des accès aux assiettes : "+history.resume());
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (UnusableEntryException e) {
      e.printStackTrace();
    } catch (TransactionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    say("stop cleaning");
  }
  /**
   * Affiche un message sur la sortie standard préfixé par le numéro du philosophe
   * @param message le message à afficher
   */
  public void say(String message){
    System.out.println(prefix+"P" + phi_id + ": "+message); 
  }
  
  /**
   * Lancement du programme sur un noeud
   * @param args args[0]= numéro du philosophe sur ce noeud
   *             args[1]= nombre total de philosophes
   */
  public static void main(String[] args) {
    
    JavaSpace space = SpaceAccessor.getSpace();

    int id = Integer.parseInt(args[0]);
    int num = Integer.parseInt(args[1]);

    Philosopher philosopher = new Philosopher(space, id, num);
    philosopher.working();
    philosopher.cleaning();
  }

}
