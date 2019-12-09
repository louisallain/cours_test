package echo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Echo extends Remote{
  
  public static final String host="bugs5"; // hote du serveur
  public static final String name="Echo";
  public static final int port = 1099;
  
  public Object echo(Object object) throws RemoteException;
}
