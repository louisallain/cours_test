package sensor;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TempListener extends UnicastRemoteObject implements Observer {

  protected TempListener() throws RemoteException {
  }

  public void notify(double value) throws RemoteException {
    System.out.println("temperature has changed, new temperature is "+value);
  }

  public static void main(String[] args) {
    try { 
      if (System.getSecurityManager() == null) {
        System.setSecurityManager(new SecurityManager()); 
      }
      // recherche une référence vers un thermomètre distant
      Observable temp = (Observable) Naming.lookup("rmi://"+Observable.host+":"+Observable.port+"/"+Observable.name);
      // lecture de la température initiale
      System.out.println("initial temperature is "+temp.get());
      // enregistrement auprès du thermomètre distant pour être informé des changements de température
      temp.register(new TempListener());
    } catch (MalformedURLException | NotBoundException | RemoteException e) {
      System.err.println("no thermometer available : "+e);
      System.exit(1);
    } 
  }
}
