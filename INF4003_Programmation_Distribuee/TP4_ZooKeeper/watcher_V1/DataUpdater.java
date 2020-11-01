package watcher_V1;


import java.io.IOException;
import java.util.UUID;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;

public class DataUpdater implements Watcher {
  
  private static final String my_id= "/"+System.getProperty("user.name");
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static String zooDataPath = "/a";

  private int nbUpdateToDo;
  private int nbOf_ms_betweenTwoUpdate;
  
  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(DataUpdater.class);
  
  public DataUpdater(int nbUpdateToDo, int nbOf_ms_betweenTwoUpdate) {
    this.nbUpdateToDo = nbUpdateToDo;
    this.nbOf_ms_betweenTwoUpdate = nbOf_ms_betweenTwoUpdate;
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
   
  public void run() throws InterruptedException, KeeperException {
    String name = "localhost";
    try {
        name = InetAddress.getLocalHost().getHostName();
    } catch(Exception e) {
        e.printStackTrace();
    }
    
    
    for(int i = 0; i<this.nbUpdateToDo; i++) {

      String data = name + "-" + String.valueOf(i) + "-" + LocalDateTime.now();
      byte zoo_data[] = data.getBytes();
      zk.setData(my_id+zooDataPath, zoo_data, -1);
      try {
        Thread.sleep(this.nbOf_ms_betweenTwoUpdate);
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
    
    System.out.println("Usage DataUpdater nbUpdateToDo nbOf_ms_betweenTwoUpdate");
    int nb = Integer.valueOf(args[0]);
    int inter = Integer.valueOf(args[1]);
    
    DataUpdater dataUpdater = new DataUpdater(nb, inter);
    dataUpdater.run();
  }
  
}

