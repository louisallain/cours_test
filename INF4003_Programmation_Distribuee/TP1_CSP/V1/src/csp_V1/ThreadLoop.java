/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 * @date janvier 2015
 */
package csp_V1;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread exécutant une tâche itérative. 
 * La méthode {@link #startLoop()} provoque le début de l'exécution de la tâche.
 * La tâche se décompose en trois parties dont le traitement est défini dans les 
 * Les méthodes abstraites {@link #beforeLoop()}, {@link #inLoop()} et {@link #afterLoop()}.
 * 1- la méthode {@link #beforeLoop()} est exécutée, puis le thread est démarré ({@link Thread#run()}), 
 * 2- la méthode {@link #inLoop()} est exécutée itérativement tant que la méthode {@link #exitLoop()} 
 *    n'est pas appelée
 * 3- la méthode {@link #afterLoop()} est exécutée à la sortie de l'itération de la tâche, 
 *    puis le thread est définitivement arrêté.
 */
abstract class ThreadLoop extends Thread {

  /** 
   * Variable atomique pour l'arrêt du thread
   */
  private AtomicBoolean stopped; 
  /**
   * Variable atomique pour visualiser la progression de l'exécution du thread
   */
  private AtomicBoolean trace;
  
  /**
   * Initialisation de la tâche sans la démarrer
   * @param name le nom donné au thread
   */
  ThreadLoop(String name) {
    super(name);
    stopped= new AtomicBoolean(true);
    trace= new AtomicBoolean(false);
  }
  
  /**
   * Active/désactive la fonction de trace
   * @param enable vrai ssi la trace est active
   */
  public final void setTrace(boolean enable){
    if (enable) printOut("trace enabled");
    else printOut("trace disabled");
    trace.set(enable);
  }
  
  /**
   * Fonction d'affichage d'une information de trace si la trace est activée
   * @param message information à afficher
   */
  public final void trace(String message){
    if (trace.get()) printOut(message);
  }

  /**
   * Fonction d'affichage sur la sortie standard d'une chaîne précédée du numéro de processus 
   * @param string chaîne à afficher sur la sortie standard 
   */
  public final void printOut(String string){
    System.out.println("["+getName()+"]: "+string);
  }

  /**
   * Fonction d'affichage sur la sortie d'erreur d'une chaîne précédée du numéro de processus 
   * @param string chaîne à afficher sur la sortie d'erreur 
   */
  public final void printErr(String string){
    System.err.println("**** ["+getName()+"]: "+string);
  }
  
  /**
   * Démarrage de la boucle du thread
   * Exécute le code précédant l'itération ({@link #beforeLoop()}, 
   * puis appelle la méthode start() du Thread qui réalise la boucle ({@link #inLoop()}
   */
  public final void startLoop(){
    trace("starting");
    try {
      Thread.sleep(new Random(System.currentTimeMillis()).nextInt(1000)); // temps de démarrage aléatoire
    } catch (InterruptedException ignore) {
    }
    trace("entering beforeLoop()");
    beforeLoop();
    trace("exiting beforeLoop()");
    stopped.set(false);
    super.start();
  }

  /**
   * Sortie de la boucle d'exécution du thread
   */
  public final void exitLoop(){
    stopped.set(true);
    trace("exiting");
  }

  /**
   * Boucle itérative sur le code de l'itération ({@link #inLoop()}),
   * puis exécute le code suivant l'itération {@link #afterLoop()}.
   * @see java.lang.Thread#run()
   */
  public final void run(){
    
    while(! stopped.get()){
      trace("entering inLoop()");
      inLoop();
      trace("exiting inLoop()");
    }
    trace("entering afterLoop()");
    afterLoop();
    trace("exiting afterLoop()");
  }

  /**
   * Tâche à exécuter avant la boucle d'exécution, 
   * c-à-d. avant la première exécution de {@link #inLoop()}
   */
  abstract void beforeLoop();
  
  /**
   * Tâche à exécuter pendant la boucle d'exécution
   */
  abstract void inLoop();
  
  /**
   * Tâche à exécuter après la boucle d'exécution, 
   * c-à-d. après la dernière exécution de {@link #inLoop()}
   */
  abstract void afterLoop();
  
}
