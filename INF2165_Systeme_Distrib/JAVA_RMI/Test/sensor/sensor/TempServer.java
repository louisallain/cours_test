/**
 * @author raimbaul
 */
package sensor;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class TempServer {

  public static void main(String[] args) 
      throws MalformedURLException, RemoteException, UnknownHostException {
    
    System.out.println("[temp server] starting on "+InetAddress.getLocalHost().getHostName());
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager()); 
    }
    try { 
      LocateRegistry.createRegistry(Observable.port); 
    } catch(Exception e) { System.err.println("[temp server] rmiregistry already started");
    }
    Observable sensor= new TempServant();
    Naming.rebind(Observable.name,sensor); 
    System.out.println("[temp server] temp server ready");
  }
}
