package dshell;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintNode implements Watcher {
  
  private static final String my_id= "/e1602246";
  private static String hostPort = "dmis:2181";
  private static final String zooDataPath = "/node_map";
  
  byte zoo_data[] = null;
  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(PrintNode.class);
  
    public PrintNode() {
    
        try {
            zk = new ZooKeeper(hostPort, 2000, this);
            if (zk != null) {

                InetAddress ip;
                String hostname = "default";

                try {
                    ip = InetAddress.getLocalHost();
                    hostname = ip.getHostName();
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                this.createZnodePersistant(my_id+zooDataPath, hostname);
                System.out.println("Writen on zookeeper node :" + hostname);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createZnodePersistant(String znodePath, String data){
        try {
            if (zk.exists(znodePath, this) == null){
                zk.create(znodePath, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT_SEQUENTIAL);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
  
    @Override
    public void process(WatchedEvent event) {
        
    }
  
    public static void main(String[] args) {

        new PrintNode();
    }
  
}


