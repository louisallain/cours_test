/**
 * 
 */
package watcher_V1;

import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataWatcher implements Watcher, Runnable {
  
  private static final String my_id= "/"+System.getProperty("user.name");
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static final String zooDataPath = "/a";
  
  byte zoo_data[] = null;
  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(DataWatcher.class);
  
  public DataWatcher() {
    
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
      if (zk != null) {
        try {
          if (zk.exists(my_id+zooDataPath, this) == null) {
            zk.create(my_id+zooDataPath, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
          }
        } catch (KeeperException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void printData() throws InterruptedException, KeeperException {
    
    zoo_data = zk.getData(my_id+zooDataPath, this, null);
    String zString = new String(zoo_data);
    System.out.printf("\nCurrent Data @ ZK Path %s: %s",my_id+zooDataPath, zString);
  }
  
  @Override
  public void process(WatchedEvent event) {
    
    System.out.printf("\nEvent Received: %s", event.toString());
    if (event.getType() == Event.EventType.NodeDataChanged) {
      try {
        printData();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (KeeperException e) {
        e.printStackTrace();
      }
    }
  }
  
  public void run() {
    try {
      synchronized (this) {
        while (true) {
          wait();
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }
  
  public static void main(String[] args) throws InterruptedException, KeeperException {
    
    DataWatcher dataWatcher = new DataWatcher();
    dataWatcher.printData();
    dataWatcher.run();
  }
  
}


