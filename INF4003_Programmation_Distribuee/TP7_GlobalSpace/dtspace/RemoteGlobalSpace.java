package dtspace;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGlobalSpace extends Remote {
    Tuple readIfExistsLocalSpace(Tuple m) throws RemoteException;
    Tuple takeIfExistsLocalSpace(Tuple m) throws RemoteException;
}
