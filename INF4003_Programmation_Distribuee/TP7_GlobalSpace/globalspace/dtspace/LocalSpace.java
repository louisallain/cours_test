/**
 * Implémentation distribuée d'une JavaSpace
 * Université de Bretagne Sud
 * Master 2 INFO 
 * @author F. Raimbault
 */
package dtspace;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Un espace local de tuples (pour un noeud)
 * Supporte des accès concurrents multi-thread.
 * Optimisée pour la recherche sur le premier champ.
 */
public class LocalSpace extends UnicastRemoteObject implements TupleMemory {

  /** l'ensemble de tous les tuples de cet espace */
  protected ConcurrentHashMap<Comparable, ConcurrentLinkedQueue<Tuple>> memory;
  //plutot qu'utiliser une liste on utilise une table avec comme entrée 
  //la valeur du premier champ des tuples pour accélérer les recherches de tuples
 
  
  /**
   * Un espace local de tuples
   */
    public LocalSpace() throws RemoteException {
    }
  /**
   * 
   * @see dtspace.TupleMemory#create(int)
   */
    public void create(int size) {
      memory= new ConcurrentHashMap<Comparable, ConcurrentLinkedQueue<Tuple>>(size);
      trace("tuple memory created");
    }
    
  /**
   * @see dtspace.TupleMemory#write(dtspace.Tuple)
   */
  @Override
  public void write(Tuple tuple) {
    trace("writing tuple "+tuple+" on "+dump());
    if (memory == null) create(256);
    ConcurrentLinkedQueue<Tuple> list= memory.get(tuple.getField(0));
    if (list==null){ // premier tuple avec cette valeur de premier champ
      list= new ConcurrentLinkedQueue<Tuple>();
      memory.put(tuple.getField(0), list);
    }
    list.add(tuple);
    trace("tuple "+tuple+" written");
  }
  
  /**
   * @see dtspace.TupleMemory#read(dtspace.Tuple)
   */
  @Override
  public Tuple read(Tuple m) {
    trace("reading tuple "+m+" on "+dump());
    int tempo= 0;
    while(true){
      final Tuple t= findAndRemove(m,false);
      if (t!=null) {
        trace("tuple "+t+" matching "+m+" founded");
        return t;
      }
    }
  }
  
  /**
   * @see dtspace.TupleMemory#readIfExists(dtspace.Tuple)
   */
  @Override
  public Tuple readIfExists(Tuple m) {
    trace("reading if exists "+m+" on "+dump());
    Tuple found= findAndRemove(m,false);
    if (found == null) {
      trace("tuple matching "+m+" not founded");
    }else {
      trace("tuple "+found+" matching "+m+" founded");
    }
    return found;
  }
  
  /**
   * Recherche un tuple correspondant à un motif dans une liste
   * @param m le motif
   * @param del vrai ssi suppression du tuple de l'ensemble une fois trouvé
   * @param list la liste dans laquelle rechercher
   * @return un tuple qui correspond au motif ou null si non trouvé
   */
  private Tuple findAndRemove(Tuple m, boolean del, ConcurrentLinkedQueue<Tuple> list) {
    for(Tuple t:list){
      if (t.compareTo(m)==1){ // trouvé 
        if (del){ // suppression demandée
          if (list.remove(t)) return t; // suppression réussie
          else continue; // suppression non réalisée
        }else{ // suppression non demandée
          return t;
        }
      }
    }
    return null;
  }
  /**
   * Recherche un tuple correspondant à un motif 
   * @param m le motif
   * @param del vrai ssi suppression du tuple de l'ensemble une fois trouvé
   * @return un tuple qui correspond au motif ou null si non trouvé
   */
  private Tuple findAndRemove(Tuple m, boolean del) {
    if (memory == null) {
      create(256);
    }
    final Comparable field0= m.getField(0);
    if (field0!=null){ // le premier champ du motif a une valeur 
      ConcurrentLinkedQueue<Tuple> list= memory.get(field0); // il est donc potentiellement dans cette liste
      return (list != null) ? findAndRemove(m,del,list) : null;
    }else{ // il va falloir parcourir toutes les listes de tuples !
      for (ConcurrentLinkedQueue<Tuple> list:memory.values()){
        Tuple t= findAndRemove(m,del,list);
        if (t != null) return t;
      }
    }
    return null;
  }

  /**
   * @see dtspace.TupleMemory#take(dtspace.Tuple)
   */
  @Override
  public Tuple take(Tuple m) {
    trace("taking "+m+" on "+dump());
    int tempo= 0;
    while(true){
      final Tuple t= findAndRemove(m,true);
      if (t != null) {
        trace("tuple "+t+" matching "+m+" founded and taken");
        return t;
      }
      try {
        tempo+=200;
        Thread.sleep(tempo); //temporise
      } catch (InterruptedException e) {
        return null;
      }
      trace("try again taking "+m+" on "+dump());
    }
  }
  /**
   * @see dtspace.TupleMemory#takeIfExists(dtspace.Tuple)
   */
  @Override
  public Tuple takeIfExists(Tuple m) {
    trace("taking if exists "+m+" on "+dump());
    Tuple found= findAndRemove(m,true);
    if (found == null) {
      trace("tuple matching "+m+" not founded");
    }else {
      trace("tuple "+found+" matching "+m+" founded and taken");
    }return found;
  }
  
  /**
   * @see dtspace.TupleMemory#clean()
   */
  @Override
  public void clean() throws IOException {
    trace("cleaning tuple memory");
    memory.clear();
  }

  /**
   * @see dtspace.TupleMemory#size()
   */
  @Override
  public int size() {
    if (memory == null) return 0;
    return memory.size();
  }

  /**
   * @see dtspace.TupleMemory#distroy()
   */
  @Override
  public void distroy() {
    trace("distroying tuple memory");
    memory= null;
  }
  
  /**
   * Chaîne représentant textuellement le contenu d'un espace de tuple,
   * sous la forme d'une liste {<champ1,champ2,...,>, \n ...,\n}
   * La méthode toString() est invoquée sur chaque champ
   * @see java.lang.Object#toString()
   */
  public String dump() {
    
    StringBuilder sb= new StringBuilder("{");
    if (memory != null) { 
      for(ConcurrentLinkedQueue<Tuple> list:memory.values()){
        for(Tuple t:list){
          sb.append(t.toString()).append(",");
        }
      }
    }
    if (sb.length()!=1) sb.delete(sb.length()-1, sb.length()); // enlève le dernier ",\n"
    return sb.append('}').toString();
  }
  
  /**
   * Drapeau de génération de trace d'exécution
   */
  private static boolean trace= false;
  /**
   * Activation ou non des messages de traces
   * @see dtspace.TupleMemory#setTrace(boolean)
   */
  public void setTrace(boolean value) {
    trace= value;
  }
  /**
   * Affichage d'un message sur la console suivant le niveau de trace
   * @param message 
   */
  public void trace(String message) {
    if (trace) System.out.println(message); 
  }
  

  // tests
  /*
  public static void main(String[] args) throws InterruptedException{
    
    final TupleMemory space= new LocalSpace();
    space.create(10);
    space.setTrace(true);
    new Thread("Thread1"){
      public void run() {
        space.write(new Tuple("a",new Integer(1)));
        space.write(new Tuple("a",new Integer(2)));
        space.write(new Tuple("a",new Integer(3)));
        space.take(new Tuple("a",null));
        System.out.println("Thread 1: read(a,?)="+space.read(new Tuple("a",null)));
        System.out.println("Thread 1: take(a,?)="+space.take(new Tuple("a",null)));
        System.out.println("Thread 1: read(?,?)="+space.read(new Tuple(null,null)));
     };
    }.start();
    new Thread("Thread 2"){
      public void run() {
        space.write(new Tuple("b",new Integer(2)));
        space.write(new Tuple("b",new Integer(3)));
        space.write(new Tuple("b",new Integer(4)));
        System.out.println("Thread 2: take(b,4)="+space.take(new Tuple("b",new Integer(4))));
        space.write(new Tuple("a",new Integer(3)));
        System.out.println("Thread 2: take(a,?)="+space.take(new Tuple("a",null)));
      }
    }.start();
    Thread.sleep(2000);
    space.distroy();
  }
  */
}
