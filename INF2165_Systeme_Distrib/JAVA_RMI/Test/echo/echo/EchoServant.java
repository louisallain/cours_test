package echo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class EchoServant extends UnicastRemoteObject implements Echo {

  protected EchoServant() throws RemoteException { // constructeur sans paramètre indispensable
    /* par défault invoke super() */
  }

  public Object echo(Object object) throws RemoteException{ 
    System.out.println("[echo servant] received from client: "+object);
    return "je suis là !";
  }
}
