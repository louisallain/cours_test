package csp_V2;


import java.io.IOException;
import java.util.UUID;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;

public class Barriere implements Watcher, Runnable{
  
  
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static final String zooDataPath = "/barriere";
  private static final String my_id= "/"+System.getProperty("user.name");
  private static final String barrierePath = my_id+zooDataPath;
  private int nbNodes;
  private int currentNbVotes;
  private boolean allNodesEntered = false;

  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(Barriere.class);
  
  public Barriere(int nbNodes) {

    this.nbNodes = nbNodes;

    try {

      zk = new ZooKeeper(hostPort, 2000, this);
      // créer le noeud election
      if (zk != null) {
        if (zk.exists(Barriere.barrierePath, this) == null) zk.create(Barriere.barrierePath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    } 
    catch (KeeperException | InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  private void watchForNodesInBarriere() throws InterruptedException, KeeperException {

    List<String> children = zk.getChildren(Barriere.barrierePath, this);
    
    if(this.nbNodes == children.size()) { // tout le monde est entré dans la barrière
      this.allNodesEntered = true;
      synchronized(this) {
        this.notify();
      }
    }
  }

  private void enterBarriere() {

    byte zoo_data[] = new String("0").getBytes();
    try {
      zk.create(Barriere.barrierePath+"/enter-", zoo_data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    } catch(KeeperException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void process(WatchedEvent event) {
    try {
      this.watchForNodesInBarriere();
    } catch (InterruptedException | KeeperException e) {
      e.printStackTrace();
    }
  }
   
  public void run() {

    try { 
      this.watchForNodesInBarriere(); // met un watcher une première fois
    } catch (InterruptedException | KeeperException e) {
      e.printStackTrace();
    } 
    this.enterBarriere();
    try {
      synchronized (this) {
        while (!this.allNodesEntered) {
          this.wait();
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  public void enter(String msg) {
      this.run();
      System.out.println(msg);
  }

  public static void main(String args[]) {
    Barriere b = new Barriere(Integer.parseInt(args[0]));
    b.enter("Unblocked");
  }
}

