package pollshell;


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

public class Exec implements Watcher, Runnable{
  
  
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static final String zooDataPath = "/election";
  private static final String my_id= "/"+System.getProperty("user.name");
  private static final String electionPath = my_id+zooDataPath;
  private int nbNodes;
  private int currentNbVotes;
  private boolean allNodesVoted = false;
  private boolean isLeader = false;
  private String myVote;
  private String bestVote;
  private String cmd;

  ZooKeeper zk;
  
  static final Logger LOG = LoggerFactory.getLogger(Exec.class);
  
  public Exec() {

    try {

      zk = new ZooKeeper(hostPort, 2000, this);
      // créer le noeud election
      if (zk != null) {
        if (zk.exists(this.electionPath, this) == null) zk.create(this.electionPath, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
      }
    } 
    catch (KeeperException | InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  public void setNbNodes(int nb) {
    this.nbNodes = nb;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  private void watchForVote() throws InterruptedException, KeeperException {

    List<String> children = zk.getChildren(this.electionPath, this); // donne la liste des fils sous le noeud /allain1/election
    
    if(this.nbNodes == children.size()) { // tout le monde a voté

      List<String> votes = new ArrayList<String>();

      for(String c : children) {
        String newVote = new String(zk.getData(this.electionPath+"/"+c, null, null));
        votes.add(newVote);
      }
      double bestLoadAvg = 1.0;
      long bestTimeAssociatedWithBestLoadAvg = 0;
      for(String v : votes) {
        
        double loadAvg = Exec.getLoadAvgFromVote(v);
        long time = Exec.getTimeFromVote(v);
        if(loadAvg < bestLoadAvg) {
          bestLoadAvg = loadAvg;
          bestTimeAssociatedWithBestLoadAvg = time;
          this.bestVote = new String(bestLoadAvg+"/"+bestTimeAssociatedWithBestLoadAvg);
        } 
        else if(loadAvg == bestLoadAvg) {
          if(time < bestTimeAssociatedWithBestLoadAvg) {
            bestLoadAvg = loadAvg;
            bestTimeAssociatedWithBestLoadAvg = time;
            this.bestVote = new String(bestLoadAvg+"/"+bestTimeAssociatedWithBestLoadAvg);
          }
        }
      }

      if(Exec.getLoadAvgFromVote(this.myVote) == bestLoadAvg && Exec.getTimeFromVote(this.myVote) == bestTimeAssociatedWithBestLoadAvg) {
        this.isLeader = true;
      }
      this.allNodesVoted = true;
      synchronized(this) {
        this.notify();
      }
    }
  }

  private static double getLoadAvgFromVote(String v) {
    return Double.parseDouble(Arrays.asList(v.split("/")).get(0));
  }

  private static long getTimeFromVote(String v) {
    return Long.parseLong(Arrays.asList(v.split("/")).get(1));
  }

  @Override
  public void process(WatchedEvent event) {
    try {
      this.watchForVote(); // re-regarde les votes
    } catch (InterruptedException | KeeperException e) {
      e.printStackTrace();
    }
  }
   
  public void run() {

    try { 
      this.watchForVote(); // met un watcher une première fois
    } catch (InterruptedException | KeeperException e) {
      e.printStackTrace();
    } 
    this.vote();
    try {
      synchronized (this) {
        while (!this.allNodesVoted) {
          this.wait();
        }
        System.out.println("Mon vote = " + this.myVote);
        System.out.println("Vote élu = " + this.bestVote);
        if(this.isLeader) {
          try {
            Runtime.getRuntime().exec(this.cmd);
            System.out.println("Commande exécutée = " + this.cmd);
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  private void vote() {

    this.myVote = new LoadAvg().getLoadAvgOfLastMinut() + "/" + String.valueOf(System.currentTimeMillis());
    byte zoo_data[] = this.myVote.getBytes();
    try {
      zk.create(this.electionPath+"/vote-", zoo_data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    } catch(KeeperException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws InterruptedException, KeeperException {
    
    if(args.length != 2) {
      System.out.println("Usage : exec nbTotalNodes cmd");
      return;
    }

    int nb = Integer.valueOf(args[0]); // nombre de noeuds total participant à l'élection
    String cmd = args[1];


    Exec exec = new Exec();
    exec.setNbNodes(nb);
    exec.setCmd(cmd);
    exec.run();
  }
  
}

