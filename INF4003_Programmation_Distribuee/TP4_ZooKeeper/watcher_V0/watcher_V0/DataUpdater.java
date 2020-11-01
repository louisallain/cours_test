package watcher_V0;


import java.io.IOException;
import java.util.UUID;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUpdater implements Watcher {
  
  private static final String my_id= "/"+System.getProperty("user.name");
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static String zooDataPath = "/a";
  
  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
  
  public DataUpdater() {
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
   
  public void run() throws InterruptedException, KeeperException {
    
    while (true) {
      String uuid = UUID.randomUUID().toString();
      byte zoo_data[] = uuid.getBytes();
      zk.setData(my_id+zooDataPath, zoo_data, -1);
      try {
        Thread.sleep(3000); // Sleep for 3 secs
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
  
  @Override
  public void process(WatchedEvent event) {
    
    System.out.printf("\nEvent Received: %s", event.toString());
  }

  public static void main(String[] args) throws InterruptedException, KeeperException {
    
    DataUpdater dataUpdater = new DataUpdater();
    dataUpdater.run();
  }
  
}

