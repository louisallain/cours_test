/**
 * @author raimbaul
 */
package sensor;

import java.rmi.Naming;
import java.rmi.activation.Activatable;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupDesc;
import java.rmi.activation.ActivationGroupID;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

public class ActiveTempServer {

  // nécessite de 1) lancer rmid 2) lancer rmiregistry 3) copier sensor/ActiveTempServant.class
  public static void main(String[] args) throws Exception {
    
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager()); 
    }
    try { 
      LocateRegistry.createRegistry(Observable.port); 
    } catch(Exception e) { 
      System.err.println("warning: rmiregistry already started");
    }
    Properties env= new Properties();
    env.put("java.security.policy", System.getProperty("user.home")+"/.java.policy");
    ActivationGroupDesc group_desc= new ActivationGroupDesc(env, null);
    ActivationGroupID group_id= ActivationGroup.getSystem().registerGroup(group_desc);
    ActivationDesc obj_desc= new ActivationDesc(
        group_id,
        "sensor.ActiveTempServant", 
        "file:"+System.getProperty("user.home")+"/RMI-SENSOR", 
        // il faut y placer sensor/ActiveTempServant.class
        null);
    Observable sensor= (Observable) Activatable.register(obj_desc);
    Naming.rebind(Observable.name,sensor); 
  }
}
