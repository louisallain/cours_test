/**
 * Bibliothèque de communication événementielle
 * @author F. Raimbault
 * @date janvier 2015
 */
package csp_V3;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.conf.Configuration;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Construction et accès aux liens de communication entre un processus et ses voisins.
 * Chaque lien de communication est une socket dont les informations sont accessibles 
 * par le numéro du processus voisin.
 */
public final class Neighbouring{
  
  public static Configuration conf = new YarnConfiguration();

  ZooKeeper zk;
  byte zoo_data[] = null;
  private static String hostPort = "dmis:2181";

  /**
   * la table des sockets
   */
  private HashMap<Integer, DatagramSocket> socket_map;
  /**
   * La table des adresses de destination
   */
  private HashMap<Integer, InetAddress> address_map;
  /**
   * la table des ports de destination
   */
  private HashMap<Integer, Integer> port_map;
  /**
   * la table des écouteurs créés (pour chaque socket)
   */
  private HashMap<Integer, MessageListener> listener_map;
  /**
   * Le processus associé à ce voisinage
   */
  private ConcurrentProcess process;
  /**
   * Nombre de voisins
   */
  private int neighbour_cnt;
  /**
   * Offset ajouté au numéro de port pour pouvoir travailler à plusieurs sur les 
   * même noeuds
   */
  int offset;
  
  /**
   * Nouveau voisinage d'un processus
   * @param process le processus concerné
   * @param offset valeurs systématiquement ajoutées aux ports (locaux et distants)
   */
  Neighbouring(ConcurrentProcess process, int offset){
    this.process= process;
    this.offset= offset;
    socket_map= new HashMap<Integer, DatagramSocket>(10);
    address_map= new HashMap<Integer, InetAddress>(10);
    port_map= new HashMap<Integer, Integer>(10);
    listener_map= new HashMap<Integer, MessageListener>(10);
    neighbour_cnt= 0;
  }
  
  /**
   * Ajout d'un lien de communication avec un processus distant. 
   * Créé une socket partagée et un écouteur sur cette socket.
   * Remarque : l'offset {@link Neighbouring#offset} est ajouté 
   * valeurs de port passées en paramètres
   * @param local_port le port de réception sur la socket 
   * @param remote_host_name le nom de l'hote du processus distant
   * @param remote_id l'identifiant du processus distant
   * @param remote_port le port de destination de la socket 
   * @throws UnknownHostException
   * @throws SocketException
   */
  void add(int local_port,String remote_host_name,int remote_id,int remote_port) 
        throws UnknownHostException, SocketException{
    port_map.put(remote_id, remote_port+offset);
    final InetAddress host_address= InetAddress.getByName(remote_host_name);
    final InetSocketAddress socket_address= new InetSocketAddress(host_address,remote_port+offset); 
    final InetAddress dest_address= socket_address.getAddress();
    address_map.put(remote_id, dest_address);
    final DatagramSocket socket= new DatagramSocket(local_port+offset);
    socket.setSoTimeout(1000);
    socket_map.put(remote_id, socket);
    final MessageListener listener= new MessageListener(process, socket);
    listener_map.put(remote_id, listener);
    neighbour_cnt += 1;
  }
 
  /**
   * Lecture d'un fichier décrivant la topologie complète du réseau de tous les 
   * processus participants. Mémorisation des informations relatives au voisinage de 
   * ce processus. Le fichier contient la description d'un canal de communication 
   * (bi-directionnel) par ligne sous la forme: <br>
   * nom DNS : id du processus : port local UDP : nom DNS du voisin : id du voisin : port distant UDP
   * @param filename nom du fichier
   * @throws IOException
   */
  void read(String filename) 
      throws IOException {
    HashSet<Integer> id_set= new HashSet<Integer>(10);

    Path cfgFilePath = new Path(filename);
    FileSystem fs = FileSystem.get(conf);
    InputStreamReader isr = new InputStreamReader(fs.open(cfgFilePath));
    BufferedReader reader = new BufferedReader(isr);
    
    String line= reader.readLine();
    while(line!=null){
      String[] fields= line.split(":");
      //System.out.println("line="+line);
      if ((fields != null) && (fields.length == 4)){
        try {
        
          final int id_1= Integer.parseInt(fields[0].trim());
          final int port_1= Integer.parseInt(fields[1].trim());
          
          
          final int id_2= Integer.parseInt(fields[2].trim());
          final int port_2= Integer.parseInt(fields[3].trim());

          String zkPathToHostname_1 = "/e1602246/node_map/p"+id_1;
          String zkPathToHostname_2 = "/e1602246/node_map/p"+id_2;
          String hostname_1= ""; // récupérer l'hostname depuis zookeeper
          String hostname_2= ""; // récupérer l'hostname depuis zookeeper

          try {
            zk = new ZooKeeper(hostPort, 2000, new Watcher() {
		
              public void process(WatchedEvent we) {

              }
            });
            if (zk != null) {

              try {
                  zoo_data = zk.getData(zkPathToHostname_1, false, null);
                  hostname_1 = new String(zoo_data);
                  zoo_data = zk.getData(zkPathToHostname_2, false, null);
                  hostname_2 = new String(zoo_data);
              }
              catch (KeeperException | InterruptedException e) {
                  e.printStackTrace();
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          
          System.out.println("hostname_1 =" + hostname_1);
          System.out.println("hostname_2 =" + hostname_2);

          id_set.add(id_1);
          id_set.add(id_2);
          if (id_1 == process.getMyId()){
            add(port_1, hostname_2, id_2, port_2);
          }else if (id_2 == process.getMyId()){
            add(port_2, hostname_1, id_1, port_1);
          }
        } catch (NumberFormatException e) {
          process.printErr("error on line "+line+": "+e.toString());
         } catch (UnknownHostException e){
          process.printErr("error on line "+line+": "+e.toString());
        } catch (IllegalArgumentException e){
          process.printErr("error on line "+line+": "+e.toString());
        }
      }else{
        process.printErr("warning, skip invalid line: "+line);
      }
      line= reader.readLine();
    }
    reader.close();
  }

  /**
   * Accesseur du nombre de voisins
   * @return le nombre de voisins
   */
  int size(){
    return neighbour_cnt;
  }
  
  /**
   * Accesseur de l'identité des voisins
   * @return (une COPIE de) l'ensemble des identifiants des voisins
   */
  Set<Integer> getIdentities(){
    return new HashSet<Integer>(socket_map.keySet());
  }
  
  /**
   * Démarrage des écouteurs 
   */
  void open(){
    for(MessageListener listener : listener_map.values()){
      listener.setTrace(true);
      listener.startLoop();
    }
  }
  /**
   * Fermeture de tous les liens de communication vers les voisins et 
   * arrêt des écouteurs associés
   */
  void close(){
    for(MessageListener listener : listener_map.values()){
      listener.exitLoop();
     }
  }
  
  /**
   * Accesseur de la socket d'écriture vers un processus distant
   * @param id l'identifiant du processus destinataire
   * @return la socket pour envoyer un message vers le processus id 
   *         ou null si le processus id n'est pas un voisin.
   */
  DatagramSocket getOutputSocket(int id){
    return socket_map.get(id);
  }
  
  /**
   * Accesseur de l'adresse d'un processus distant
   * @param id l'identifiant du processus destinataire
   * @return l'adresse du processus id 
   *         ou null si le processus id n'est pas un voisin.
   */
  InetAddress getOutputAddress(int id){
    return address_map.get(id);
  }
  
  /**
   * Accesseur du port d'un processus distant
   * @param id l'identifiant du processus destinataire
   * @return le port du processus id 
   *         ou null si le processus id n'est pas un voisin.
   */
  int getOutputPort(int id){
    return port_map.get(id);
  }
  
  /**
   * Représentation textuelle de l'ensemble des liens de communication vers les 
   * voisins
   * @see java.lang.Object#toString()
   */
  public String toString(){
    StringBuilder sb= new StringBuilder("Neighbouring of node "+process.getMyId()+":\n");
    for(int id:address_map.keySet()){
      sb.append(socket_map.get(id).getLocalPort()).append(" <=> ");
      sb.append(id).append(":").append(port_map.get(id)).append("\n");
    }
    return sb.toString();
  }

}
