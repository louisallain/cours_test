package sensor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Observable extends Remote {

  public static final String host = "bugs5";
  public static final int port = 1099;
  public static final String name = "Thermometer";

  public double get() throws RemoteException;
  
  public void register(Observer listener) throws RemoteException;
  
  public void unregister(Observer listener) throws RemoteException;
}
