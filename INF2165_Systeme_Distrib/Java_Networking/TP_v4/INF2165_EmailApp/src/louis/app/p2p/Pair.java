package louis.app.p2p;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;


/**
 * Cette classe le pair d'une application P2P.
 * Un pair peut recevoir et envoyer des UDP.
 */
public class Pair {

    /**
     * Répertoire principal du pair.
     */
    private Path mainDir;
    /**
     * Répertoire où sont les fichiers à envoyer du pair.
     */
    private Path toSentFilesDir;
    /**
     * Répertoire où sont les fichiers reçus par le pair.
     */
    private Path receivedFilesDir;
    /**
     * Le port TCP du pair.
     */
    private int TCPPort;
    /**
     * Le port UDP du pair.
     */
    private int UDPPort;
    /**
     * Le nom d'utilisateur du pair.
     */
    private String username;
    /**
     * Map où seront sauvegardés tous les autres pairs connus.
     * Type : nomUtilisateur - Adresse IP - Numéro de port
     */
    private Map<String, SimpleEntry<InetAddress, Integer>> knownPairs;

    private static String IPV4_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

    /**
     * Créer un nouveau Pair en précisant le chemin de son dossier où seront les messages reçus et 
     * les messages à envoyer.
     * Initialise l'arborescence de dossier suivante :
     * --- P2P_Files
     * ------ toSentFiles
     * --------- [nom_user1]
     * ------------ [fichier_à_envoyer_à_user1]
     * ...
     * ------ receivedFiles (Contient tous les fichiers reçus.)
     * A noter que les sous-dossiers de toSentFiles et les fichiers contenus dans ces sous-dossiers seront placés par une application externe.
     * @param dir le chemin du dossier du Pair où seront les messages reçus et les messages à envoyer
     * @param TCPPort le port TCP du pair
     * @param UDPPort le port UDP du pair
     * @param username le nom d'utilisateur du pair
     */
    public Pair(String dir, int TCPPort, int UDPPort, String username) {

        this.mainDir = Paths.get(dir);
        this.toSentFilesDir = Paths.get(mainDir.toString(), "toSentFiles");
        this.receivedFilesDir = Paths.get(mainDir.toString(), "receivedFiles");
        this.mainDir.toFile().mkdirs();
        this.toSentFilesDir.toFile().mkdirs();
        this.receivedFilesDir.toFile().mkdirs();
        this.TCPPort = TCPPort;
        this.UDPPort = UDPPort;
        this.username = username;
        this.knownPairs = new HashMap<String, SimpleEntry<InetAddress, Integer>>();
    }

//    /**
//     * Met en place une surveillance sur le dossier et les sous-dossiers qui contiennent les fichiers à envoyer à d'autres pairs.
//     * @param watcher le "watcher associé"
//     * @return vrai si la surveillance est active, faux sinon
//     */
//    protected void watchToSentFiles(WatchService watcher) {
//
//    	// Surveille le dosser "toSentFiles" ainsi que ses sous-dossiers
//    	// Attention on considère qu'un fichier doit être envoyé uniquement s'il est créé
//    	// et non pas juste modifié
//
//    	try(Stream<Path> paths = Files.walk(this.toSentFilesDir)) {
//
//    		paths
//    		.filter(Files::isDirectory)
//    		.forEach(p -> {
//    			try {
//    				p.register(watcher, ENTRY_CREATE);
//    			}
//    			catch(Exception e) {
//    				e.printStackTrace();
//    			}
//    		});
//    	}
//    	catch(Exception e) {
//    		e.printStackTrace();
//    	}
//    }
    
    /**
     * Donne une représentatin du Pair.
     * @return une chaine de caractère donnant l'IP, le port UDP et le port TCP du pair.
     */
    public String toString() {
        return "Username : " + this.username + "\n Port UDP : " + this.UDPPort + "\n Port TCP : " + this.TCPPort + "\n";
    }

    /**
     * Retourne le nom d'utilisateur du Pair
     * @return le nom d'utilisateur du Pair
     */
    public String getUsername() { return this.username; }
    
    /**
     * Retourne le chemin vers le dossier d'envoi du Pair.
     * @return le chemin vers le dossier d'envoi du Pair.
     */
    public Path getToSendFilesDir() {
        return this.toSentFilesDir;
    }

    /**
     * Retourne le chemin vers le dossier de réception du Pair.
     * @return le chemin vers le dossier de réception du Pair.
     */
    public Path getReceivedFilesDir() {
        return this.receivedFilesDir;
    }
    
    /**
     * Retourne une Map contenant tous les autres pairs connu par le Pair courant.
     * @return une Map contenant tous les autres pairs connu par le Pair courant.
     */
    public Map<String, SimpleEntry<InetAddress, Integer>> getKnownPairs() {
        return this.knownPairs;
    }
    
    /**
     * Retourne le port UDP du Pair.
     * @return le port UDP du pair.
     */
    public int getUDPPort() {
        return this.UDPPort;
    }

    /**
     * Retourne le port TCP du Pair.
     * @return le port TCP du Pair.
     */
    public int getTCPPort() {
        return this.TCPPort;
    }

    /**
     * Retourne un message de type 'offer' en fonction de paramètres donnés.
     * Les spécifications du type 'offer' sont donnés dans le TP.
     * @param ip l'adresse IPv4 de l'envoyeur
     * @param port le port UDP de l'envoyeur
     * @param usernameLength la taille du nom d'utilisateur du récepteur
     * @param username le nom d'utilisateur du récepteur
     * @param filenameLength la taille du nom du fichier à envoyer
     * @param filename le nom du fichier à envoyer
     * @return un tableau d'octets correspondant à un message de type 'offer'
     */
    public static byte[] buildOfferMessageBytes(InetAddress ip, int port, int usernameLength, String username, int filenameLength, String filename) throws IOException {

        // construit le message
        byte code = 1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(ByteBuffer.allocate(1).put(code).array()); // code 
        bos.write(ByteBuffer.allocate(4).put(ip.getAddress()).array()); // adresse IP
        bos.write(ByteBuffer.allocate(2).putShort(new Integer(port).shortValue()).array()); // port udp
        bos.write(ByteBuffer.allocate(1).put(new Integer(usernameLength).byteValue()).array()); // taille du nom_user2
        bos.write(ByteBuffer.allocate(usernameLength).put(username.getBytes()).array()); // nom_user2
        bos.write(ByteBuffer.allocate(1).put(new Integer(filenameLength).byteValue()).array()); // taille nom_fichier
        bos.write(ByteBuffer.allocate(filenameLength).put(filename.getBytes()).array()); // nom_fichier

        return bos.toByteArray();
    }

    /**
     * Retourne un message de type 'request' en fonction de paramètres donnés.
     * Les spécifications du type 'request' sont donnés dans le TP.
     * @param ip l'adresse IPv4 de l'envoyeur
     * @param port le port TCP de l'envoyeur
     * @param usernameLength la taille du nom d'utilisateur de l'envoyeur
     * @param username le nom d'utilisateur de l'envoyeur
     * @param filenameLength la taille du nom du fichier à recevoir
     * @param filename le nom du fichier à recevoir
     * @return un tableau d'octets correspondant à un message de type 'request'
     */
    public static byte[] buildRequestMessageBytes(InetAddress ip, int port, int usernameLength, String username, int filenameLength, String filename) throws IOException {
        

        // construit le message
        byte code = 2;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(ByteBuffer.allocate(1).put(code).array()); // code 
        bos.write(ByteBuffer.allocate(4).put(ip.getAddress()).array()); // adresse IP
        bos.write(ByteBuffer.allocate(2).putShort(new Integer(port).shortValue()).array()); // port udp
        bos.write(ByteBuffer.allocate(1).put(new Integer(usernameLength).byteValue()).array()); // taille du nom_user2
        bos.write(ByteBuffer.allocate(usernameLength).put(username.getBytes()).array()); // nom_user2
        bos.write(ByteBuffer.allocate(1).put(new Integer(filenameLength).byteValue()).array()); // taille nom_fichier
        bos.write(ByteBuffer.allocate(filenameLength).put(filename.getBytes()).array()); // nom_fichier

        return bos.toByteArray();
    }

    /**
     * Retourne un message de type 'delete' en fonction des paramètres donnés.
     * @param usernameLength la taille du nom d'utilisateur de l'envoyeur
     * @param username le nom d'utilisateur de l'envoyeur
     * @param filenameLength la taille du nom du fichier à supprimer
     * @param filename le nom du fichier à supprimer
     * @return un tableau d'octets correspondant à un message de type 'delete'
     * @throws IOException
     */
    public static byte[] buildDeleteMessageBytes(int usernameLength, String username, int filenameLength, String filename) throws IOException {

        // construit le message
        byte code = 3;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(ByteBuffer.allocate(1).put(code).array()); // code 
        bos.write(ByteBuffer.allocate(1).put(new Integer(usernameLength).byteValue()).array()); // taille du nom_user2
        bos.write(ByteBuffer.allocate(usernameLength).put(username.getBytes()).array()); // nom_user2
        bos.write(ByteBuffer.allocate(1).put(new Integer(filenameLength).byteValue()).array()); // taille nom_fichier
        bos.write(ByteBuffer.allocate(filenameLength).put(filename.getBytes()).array()); // nom_fichier

        return bos.toByteArray();
    }

    /**
     * Retourne un message de type 'beacon' en fonction de paramètres donnés.
     * @param ip l'adresse IPv4 de l'envoyeur
     * @param udpPort le port UDP de l'envoyeur
     * @param usernameLength la taille du nom d'utilisateur de l'envoyeur
     * @param username le nom d'utilisateur de l'enovyeur
     * @return un tableau d'octets correspondant à un message de type 'beacon'
     */
    public static byte[] buildBeaconMessageBytes(InetAddress ip, int udpPort, int usernameLength, String username) throws IOException {

        // construit le message
        byte code = 4;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(ByteBuffer.allocate(1).put(code).array()); // code
        bos.write(ByteBuffer.allocate(4).put(ip.getAddress()).array()); // adresse IP
        bos.write(ByteBuffer.allocate(2).putShort(new Integer(udpPort).shortValue()).array()); // port udp
        bos.write(ByteBuffer.allocate(1).put(new Integer(usernameLength).byteValue()).array()); // taille du nom d'utilisateur
        bos.write(ByteBuffer.allocate(usernameLength).put(username.getBytes()).array()); // nom d'utilisateur

        return bos.toByteArray();
    }

    /**
     * Lance le pair.
     */
    public void launch() {

        // Message d'accueil
        System.out.println(this.toString());

        // Lance un serveur TCP en arrière plan permettant de recevoir des fichiers
        // sur le port TCP du pair
        new louis.app.file.Serveur().launch(this.TCPPort, receivedFilesDir.toString());

        try {
            new ReceiverThread(this).start();
            new DiscoveryThread(this).start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Donne l'utilisation normale de la méthode principale de la classe.
     */
    private static String getUsage() {
        return "Usage : P2P username userDirectoryPath UDPPort TCPPort";
    }
    public static void main(String[] args) throws IOException {

        if(args.length != 4) {
            System.out.println(getUsage());
            return;
          }
      
          if(args[0].equals("-h")) {
            System.out.println(getUsage());
            return;
          }
      
          new Pair(args[1], Integer.valueOf(args[3]), Integer.valueOf(args[2]), args[0]).launch();
      }
}