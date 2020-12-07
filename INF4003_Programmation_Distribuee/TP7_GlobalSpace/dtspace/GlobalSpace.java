/**
 * Implémentation distribuée d'une JavaSpace
 * Université de Bretagne Sud
 * @author F. Raimbault
 */
package dtspace;

import java.net.InetAddress;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.rmi.*;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Espace de tuple partagé entre plusieurs noeuds
 * Exploite un espace de tuple local.
 * Toutes les écritures de tuple sont locales.
 * Aucun tuple n'est dupliqué.
 * Met en oeuvre des requêtes de motifs et des réponses de tuples 
 * entre les noeuds quand les tuples ne sont pas trouvés localement.
 */
public class GlobalSpace extends LocalSpace implements RemoteGlobalSpace {
  
  private int nbNodes;

  private ZooKeeper zk;
  private byte zoo_data[];
  private static String hostPort = "dmis:2181"; 
  private static String hostnamesZkPath = "/e1602246/node_map";

  private static int rmiPort = 1099;
  private static String rmiName = "GlobalSpace";

  /**
   * Espace local de tuples du noeud courant
   * @throws RemoteException
   */
  public GlobalSpace() throws RemoteException {

    // Creation et installation du manager de securite
    // mettre dans ~/.java.policy : grant{Permission java.security.AllPermission;};
    if (System.getSecurityManager() == null) {
        System.setSecurityManager(new SecurityManager()); 
        // ou donner mettre -Djava.security.policy=monfich.policy au lancement du serveur
    }
    // lancement du registry 
    // (ou bien en ligne de commande par: rmiregistry -J-Dclasspath=$PWD 1099)
    try { 
      LocateRegistry.createRegistry(rmiPort); 
      System.out.println("RMIregistry started on " + InetAddress.getLocalHost().getHostName());
    } catch(Exception e) {
       System.err.println("RMIregistry already started");
    }
    try {
      // enregistrement du serveur auprès du RMIregistry
      Naming.bind(rmiName, this);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Fixe le nombre de noeuds.
   * @param nbNodes le nombre de noeuds créant un espace de mémoire partagé.
   */
  public void setNbNodes(int nbNodes) {
    this.nbNodes = nbNodes;
  }

  /**
   * Donne la liste des noms d'hôtes participant à la mémoire partagée.
   * Accède à ces données depuis ZooKeeper.
   * @return la liste des noms d'hôtes participant à la mémoire partagée.
   */
  public ArrayList<String> getAllNodesHostname() {

    ArrayList<String> children = null;
    ArrayList<String> hostnames = null;

    try {

      zk = new ZooKeeper(hostPort, 2000, new Watcher() {public void process(WatchedEvent we) {}});

      if(zk != null) {
        
        if (zk.exists(hostnamesZkPath, this) != null) {

          children = zk.getChildren(hostnamesZkPath, false);
          for(String path : children) {
            hostnames.add(zk.getData(path, false, null));
          }
        }
      }
    } 
    catch (KeeperException | InterruptedException e) {
      e.printStackTrace();
    }

    return hostnames;
  }
  
  /**
   * Méthode utilisée par d'autres noeuds pour lire un tuple depuis la mémoire local d'un noeud (non bloquant).
   * @param m le tuple recherché.
   * @return le tuple correspondant ou null s'il n'existe pas.
   * @throws RemoteException 
   */
  public Tuple readIfExistsLocalSpace(Tuple m) throws RemoteException {
    return super.readIfExists(m);
  }

  /**
   * Méthode utilisée par d'autres noeuds pour retirer un tuple depuis la mémoire local d'un noeud (non bloquant).
   * @param m le tuple recherché.
   * @return le tuple correspondant ou null s'il n'existe pas.
   * @throws RemoteException 
   */
  public Tuple takeIfExistsLocalSpace(Tuple m) throws RemoteException {
    return super.takeIfExists(m);
  }

  /**
   * Lecture bloquante d'un tuple. 
   * Recherche locale puis globale si nécessaire
   * @see dtspace.LocalSpace#read(dtspace.Tuple)
   */
  public Tuple read(Tuple m) {
    int tempo= 0; // durée de temporisation avant de relancer une requête
    while(true){
      Tuple t= readIfExists(m); // recherche locale puis globale
      if (t != null){// si trouvé on arrête
        return t; 
      }else{ // on temporise avant de recommencer
        try {
          tempo += 200; // on temporise de plus en plus
          Thread.sleep(tempo); // temporisation
        } catch (InterruptedException e) {
          return null;
        }
      }
    }
  }

  /**
   * Ecriture d'une tuple (dans l'espace de tuple local)
   * @see dtspace.LocalSpace#write(dtspace.Tuple)
   */
  public void write(Tuple t){
    super.write(t);
  }
  
  /**
   * Lecture non bloquante d'un tuple. 
   * Recherche locale puis globale si nécessaire
   * @see dtspace.LocalSpace#read(dtspace.Tuple)
   */
  public Tuple readIfExists(Tuple m)  {
    Tuple t= super.readIfExists(m); // recherche locale
    if (t != null){// trouvé localement on arrête
      return t; 
    }
    else{ // recherche globale
      ArrayList<String> hostnames = this.getAllNodesHostname();
      for(String host : hostnames) {
        try {
          Remote r = Naming.lookup("rmi://"+host+":"+rmiPort+"/"+rmiName);
          System.out.println(r);
          if(r instanceof RemoteGlobalSpace) {
            t = ((RemoteGlobalSpace) r).readIfExistsLocalSpace(m);
            if(t != null) return t;
          }
  
        } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
        }
      }

      return t;
    }
  }
  
  /**
   * Extraction bloquante d'un tuple. 
   * Recherche locale puis globale si nécessaire
   * @see dtspace.LocalSpace#read(dtspace.Tuple)
   */
  public Tuple take(Tuple m) {
    int tempo= 0;// durée de temporisation avant de relancer une requête
    while(true){
      Tuple t= takeIfExists(m); // recherche locale puis globale
      if (t != null){// trouvé on arrête
        return t; 
      }
      else{ // on temporise avant de recommencer
        try {
          tempo += 200; // on temporise de plus en plus
          Thread.sleep(tempo); // temporisation
        } catch (InterruptedException e) {
          return null;
        }
      }
    }
  }

  /**
   * Extraction non bloquante d'un tuple. 
   * Recherche locale puis globale si nécessaire
   * @see dtspace.LocalSpace#read(dtspace.Tuple)
   */
  public Tuple takeIfExists(Tuple m) {
    Tuple t= super.takeIfExists(m); // recherche locale
    if (t != null){// trouvé localement on arrête
      return t; 
    }
    else{// recherche globale
      ArrayList<String> hostnames = this.getAllNodesHostname();
      for(String host : hostnames) {
        try {
          Remote r = Naming.lookup("rmi://"+host+":"+rmiPort+"/"+rmiName);
          System.out.println(r);
          if(r instanceof RemoteGlobalSpace) {
            t = ((RemoteGlobalSpace) r).takeIfExistsLocalSpace(m);
            if(t != null) return t;
          }
  
        } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
        }
      }

      return t;
    }
  }

  @Override
  /**
   * Fermeture de l'espace. 
    */
  public void distroy() {
      super.distroy();
    /********* A COMPLETER **********/
  }

  public static void main(String[] args) {
    System.out.println("OK");
    
    
    Tuple t = new Tuple(Integer.valueOf(1));

    if(args[0].equals("p")) {
      System.out.println("Producteur");
      try {
        GlobalSpace gspace = new GlobalSpace();
        gspace.write(t);
      }
      catch(RemoteException e) {
        e.printStackTrace();
      }
      
    }
    else if(args[0].equals("c")) {
      System.out.println("Consommateur");
      String host = "portia";
      int port = 1099;
      String name = "GlobalSpace";

      try {
        Remote r = Naming.lookup("rmi://"+host+":"+port+"/"+name);
        System.out.println("server found on "+host);
        System.out.println(r);
        if(r instanceof RemoteGlobalSpace) {
          Tuple ret = ((RemoteGlobalSpace) r).readIfExistsLocalSpace(t);
          System.out.println("Tuple = " + ret);
        }

      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("cannot found a server on "+host);
        System.exit(1);
      }
    }
  }
}
