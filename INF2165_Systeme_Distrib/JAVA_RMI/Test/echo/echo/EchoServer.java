package echo;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

class EchoServer {
  
  public static void main(String[] args) throws Exception{
    System.out.println("[echo server] starting on "+InetAddress.getLocalHost().getHostName());
    // Creation et installation du manager de securite
    // mettre dans ~/.java.policy : grant{Permission java.security/AllPermission;};
    if (System.getSecurityManager() == null) {
        System.setSecurityManager(new SecurityManager()); 
        // ou donner mettre -Djava.security.policy=monfich.policy au lancement du serveur
    }
    // lancement du registry 
    // (ou bien en ligne de commande par: rmiregistry -J-Dclasspath=$PWD 1099)
    try { 
      LocateRegistry.createRegistry(Echo.port); 
      System.out.println("[echo server] RMIregistry started");
    } catch(Exception e) {
       System.err.println("[echo server] RMIregistry already started");
    }
    // création d'un objet distant
    Echo servant = new EchoServant();
    // enregistrement du serveur auprès du RMIregistry
    Naming.bind(Echo.name,servant);
    System.out.println("[echo server] Echo server ready");
  }

}
