/**
 * Classe et méthodes utiles pour partager un JavaSpace
 * entre plusieurs utilisateurs
 * @author F. Raimbault
 */
package allain1.philosophers_V0;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

/**
 * Entrée propre à un seul utilisateur 
 * (permet de partager un même JavaSpace entre plusieurs utilisateurs) 
 */
public class MyEntry implements Entry {
  
  private static final long serialVersionUID= 1L; // ne pas tenir compte des changements de classe

  public String myId;

  public MyEntry(){
    myId= System.getProperty("user.name");
  }
  
  /**
   * Supprime toutes les entrées d'un utilisateur de la JavaSpace
   * @throws RemoteException
   * @throws UnusableEntryException
   * @throws TransactionException
   * @throws InterruptedException
   */
  public static void clean() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException{
    JavaSpace space = SpaceAccessor.getSpace();
    Entry tpl= space.snapshot(new MyEntry());
    while(true){
      MyEntry e= (MyEntry) space.take(tpl, null, 2*1000);
      if (e==null) break;
      System.out.println("entry found and removed: "+"\""+e.toString()+"\"");
    }
    System.out.println("no more entry found, bye.");
  }
  
  /**
   * Liste toutes les entrées d'un utilisateur présentes dans la JavaSpace
   * @throws RemoteException
   * @throws UnusableEntryException
   * @throws TransactionException
   * @throws InterruptedException
   */
  public static void list() throws RemoteException, UnusableEntryException, TransactionException, InterruptedException{
    JavaSpace space = SpaceAccessor.getSpace();
    Entry tpl= space.snapshot(new MyEntry());
    List<MyEntry> founded= new LinkedList<MyEntry>();
    while(true){ // extrait les entrées de la mémoire
      MyEntry e= (MyEntry) space.take(tpl, null, 5*1000);
      if (e==null) break;
      founded.add(e); // sauvegarde de l'entrée
      System.out.println("entry extracted: "+"\""+e.toString()+"\"");
    }
    System.out.println("no more entry found");
    if (! founded.isEmpty()){
      System.out.print("entries rewritting...");
      for(MyEntry e:founded){ // remet les entrées dans la mémoire
        space.write(e,null,Lease.FOREVER);
      }
      System.out.println("... done, bye.");
    }
  }
  
  /**
   * Execute la methode list() ou clean() suivant le paramètre args[0]
   * @param args "clean" => clean() exécutée, "list" => list() exécutée
   * @throws RemoteException
   * @throws UnusableEntryException
   * @throws TransactionException
   * @throws InterruptedException
   */
  public static void main(String[] args) throws RemoteException, UnusableEntryException, TransactionException, InterruptedException {
    
    if (args.length == 1){
      if (args[0].equals("list")) MyEntry.list();
      else if (args[0].equals("clean")) MyEntry.clean();
      else System.err.println("unknown parameter (should be clean|list)");
    } else System.err.println("missing parameter (clean|list)");
  }

  
}

