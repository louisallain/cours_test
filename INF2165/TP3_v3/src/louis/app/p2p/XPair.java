package louis.app.p2p;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.file.WatchEvent;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import javafx.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Cette classe le pair d'une application P2P.
 * Un pair peut recevoir et envoyer des UDP.
 */
public class XPair {

    private boolean run = true;

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
     */
    private Map<String, Pair<InetAddress, Integer>> knownPairs;

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
     * @param uername le nom d'utilisateur du pair
     */
    public XPair(String dir, int TCPPort, int UDPPort, String username) {

        this.mainDir = Paths.get(dir);
        this.toSentFilesDir = Paths.get(mainDir.toString(), "toSentFiles");
        this.receivedFilesDir = Paths.get(mainDir.toString(), "receivedFiles");
        this.mainDir.toFile().mkdirs();
        this.toSentFilesDir.toFile().mkdirs();
        this.receivedFilesDir.toFile().mkdirs();
        this.TCPPort = TCPPort;
        this.UDPPort = UDPPort;
        this.username = username;
        this.knownPairs = new HashMap<String, Pair<InetAddress, Integer>>();
    }

    /**
     * Met en place une surveillance sur le dossier et les sous-dossiers qui contiennent les fichiers à envoyer à d'autres pairs.
     * @param watcher le "watcher associé"
     * @return vrai si la surveillance est active, faux sinon
     */
    private void watchToSentFiles(WatchService watcher) {

        // Surveille le dosser "toSentFiles" ainsi que ses sous-dossiers
        // Attention on considère qu'un fichier doit être envoyé uniquement s'il est créé
        // et non pas juste modifié

        try(Stream<Path> paths = Files.walk(this.toSentFilesDir)) {

        paths
            .filter(Files::isDirectory)
            .forEach(p -> {
                try {
                    p.register(watcher, ENTRY_CREATE);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

     /**
     * Met en place une surveillance sur le dossier en paramètre (uniquement sur la création).
     * @param watcher le "watcher associé"
     * @param dir le dossier a surveillé
     */
    public void watch(WatchService watcher, Path dir) {
        try {
            dir.register(watcher, ENTRY_CREATE);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Donne une représentatin du Pair.
     * @return une chaine de caractère donnant l'IP, le port UDP et le port TCP du pair.
     */
    public String toString() {
        return "Username : " + this.username + "\n Port UDP : " + this.UDPPort + "\n Port TCP : " + this.TCPPort + "\n";
    }
    
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
    public Map<String, Pair<InetAddress, Integer>> getKnownPairs() {
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
     * Lance le pair.
     */
    public void launch() {

        // Message d'accueil
        System.out.println(this.toString());

        // Lance un serveur TCP en arrière plan permettant de recevoir des fichiers
        // sur le port TCP du pair
        new louis.app.file.Serveur().launch(this.TCPPort, receivedFilesDir.toString());

        try {
            this.sender(this);
            this.receiver(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lance le Thread s'occupant d'envoyer les messages.
     * Un message est envoyé uniquement lors de la création d'un nouveau fichier.
     * @param pair
     * @throws UnknownHostException
     */
    public void sender(XPair pair) throws UnknownHostException {

        (new Thread() {

            @Override
            public void run() {

                // Surveille le dossier d'envoi
                WatchService watcher;
                try {
                    watcher = FileSystems.getDefault().newWatchService();
                    pair.watchToSentFiles(watcher);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return;
                }

                // On boucle sans fin en attendant un évènement provenant
                // du dossier d'envoie du pair
                while(true) {
                    // Key des évènements définis précédemment
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    // Parcours les évènements survenus (s'il y en a évidemment)
                    for(WatchEvent<?> event : key.pollEvents()) {

                        WatchEvent.Kind<?> kind = event.kind();

                        // Gère les types OVERFLOW au cas où
                        if(kind == OVERFLOW) continue;
                        
                        // Traite l'event
                        WatchEvent<Path> ev = (WatchEvent<Path>)event;
                        // Le nom du fichier est le context de l'event
                        Path newFile = ev.context();
                        // Retrouve le chemin complet
                        Path tmp = (Path)key.watchable();
                        Path newFileFullPath = tmp.resolve(newFile);

                        // cas où c'est un nouveau dossier utilisateur
                        // ajoute ce dossier à la liste des dossiers sous surveillance
                        // on demande aussi ses coordonnées (IPV4 + n° port UDP)
                        File tmpFile = Paths.get(pair.getToSendFilesDir().toString(), newFile.toString()).toFile();
                        if(tmpFile.isDirectory() && tmpFile.exists()) {

                            // On détecte un nouvel utilisateur (ie : un nouveau dossier)
                            // On demande donc via la console des informations nécessaires
                            // TODO : REMPLACER PAR UN DISCOVERY
                            System.out.println("Nouveau dossier utilisateur au nom de " + newFile.toString());
                            Scanner sc = new Scanner(System.in);
                            
                            System.out.println("Veuillez saisir son adresse IP (sous la forme X.X.X.X) ");
                            String tmpAddr = sc.next(IPV4_PATTERN);
                            System.out.println(tmpAddr);
                            
                            System.out.println("Veuillez saisir son numéro de port UDP");
                            int tmpUDPPort = sc.nextInt();
                            System.out.println(tmpUDPPort);
                            
                            pair.watch(watcher, tmpFile.toPath());
                            try{
                                pair.getKnownPairs().put(newFile.toString(), new Pair<InetAddress, Integer>(InetAddress.getByName(tmpAddr), tmpUDPPort));
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // cas où c'est un fichier à envoyer à un autre pair
                        else if(!newFileFullPath.getParent().equals(pair.getToSendFilesDir()) && !newFileFullPath.toFile().isDirectory()) {

                            // Récupère les infos de l'utilisateur concerné
                            String user = newFileFullPath.getParent().getFileName().toString();
                            System.out.println("Nouveau fichier (" + newFile +") a envoyer a " + user);
                            InetAddress toSentIP = pair.getKnownPairs().get(user).getKey();
                            byte[] toSentIPBytes = toSentIP.getAddress();
                            int toSentPort = pair.getKnownPairs().get(user).getValue();
                            int usernameLength = user.getBytes().length;
                            int filenameLength = newFile.toString().getBytes().length;
                            
                            try {

                                DatagramSocket udpSocket = new DatagramSocket();
                                
                                // construit le message 'offer'
                                byte[] offerMsg = XPair.buildOfferMessageBytes(InetAddress.getLocalHost(), pair.getUDPPort(), usernameLength, user, filenameLength, newFile.toString());
                                
                                // envoi le message
                                DatagramPacket offer =  new DatagramPacket(offerMsg, offerMsg.length, toSentIP, toSentPort);
                                udpSocket.send(offer);
                                System.out.println("Message type 'offer' envoye a " + user);
                                Thread.sleep(50);
                            }
                            catch (SocketTimeoutException ste) {
                                System.out.println("Le pair a mis trop longtemps a répondre (Délai expire).");
                            } 
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
        }}).start();
    }

    /**
     * Créer un Thread permettant de recevoir des messages ainsi que les traiter selon le code de ses messages.
     * Les messages sont traités selon les spécifications du TP.
     * @param pair le pair concerné par la réception des messages
     */
    public void receiver(XPair pair) {

        (new Thread() {

            @Override
            public void run() {

                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(pair.getUDPPort());
                    } 
                    catch (SocketException ex) {
                        ex.printStackTrace();
                    }

                    byte receivedBytesBuffer[] = new byte[128];
                    DatagramPacket packet = new DatagramPacket(receivedBytesBuffer, receivedBytesBuffer.length);
                    
                    while (true) {
                        try {
                            
                            socket.receive(packet);
                            byte receivedData[] = packet.getData();
                            byte receivedCode = receivedData[0];
                            
                            if(receivedCode == 1) {

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                                // Adresse IP
                                bos.write(receivedData, 1, 4);
                                InetAddress receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
                                bos.reset();

                                // Port UDP
                                bos.write(receivedData, 5, 2);
                                short receivedUDPPort = ByteBuffer.wrap(bos.toByteArray()).getShort();
                                bos.reset();

                                // Taille user2name
                                byte receivedUser2NameLength = receivedData[7];
                                
                                // User2Name
                                bos.write(receivedData, 8, receivedUser2NameLength);
                                String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                                bos.reset();

                                // Taille filename
                                byte receivedFilenameLength = receivedData[7+receivedUser2NameLength+1];

                                // Filename
                                bos.write(receivedData, (7+receivedUser2NameLength+2), receivedFilenameLength);
                                String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());

                                // Traitement de la réponse
                                Path tmpPath = Paths.get(pair.getReceivedFilesDir().toString(), receivedFilename);

                                // si on n'a pas le fichier on le demande, message de type 'request'
                                if(!tmpPath.toFile().exists()) {

                                    byte[] requestMsg = XPair.buildRequestMessageBytes(InetAddress.getLocalHost(), pair.getTCPPort(), receivedUser2NameLength, receivedUser2Name, receivedFilenameLength, receivedFilename);
                                    
                                    DatagramSocket udpSocket = new DatagramSocket();
    
                                    // envoi le message
                                    DatagramPacket request =  new DatagramPacket(requestMsg, requestMsg.length, receivedIPAddr, receivedUDPPort);
                                    udpSocket.send(request);
                                    System.out.println("Message type 'request' envoye");
                                    //Thread.sleep(50);
                                }
                                // si on a déjà le fichier on demande à le supprimer, message de type 'delete'
                                else {

                                    byte[] deleteMsg = XPair.buildDeleteMessageBytes(receivedUser2NameLength, receivedUser2Name, receivedFilenameLength, receivedFilename);
                                    
                                    DatagramSocket udpSocket = new DatagramSocket();
    
                                    // envoi le message
                                    DatagramPacket delete =  new DatagramPacket(deleteMsg, deleteMsg.length, receivedIPAddr, receivedUDPPort);
                                    udpSocket.send(delete);
                                    System.out.println("Message type 'delete' envoye");
                                    //Thread.sleep(50);
                                }
                            }
                            else if(receivedCode == 2) {

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                                // Adresse IP
                                bos.write(receivedData, 1, 4);
                                InetAddress receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
                                bos.reset();

                                // Port TCP
                                bos.write(receivedData, 5, 2);
                                short receivedTCPPPort = ByteBuffer.wrap(bos.toByteArray()).getShort();
                                bos.reset();

                                // Taille user2name
                                byte receivedUser2NameLength = receivedData[7];
                                
                                // User2Name
                                bos.write(receivedData, 8, receivedUser2NameLength);
                                String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                                bos.reset();

                                // Taille filename
                                byte receivedFilenameLength = receivedData[7+receivedUser2NameLength+1];

                                // Filename
                                bos.write(receivedData, (7+receivedUser2NameLength+2), receivedFilenameLength);
                                String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());

                                // Envoi du fichier
                                new louis.app.file.Client().launch(receivedIPAddr.getHostName(), receivedTCPPPort, Paths.get(pair.getToSendFilesDir().toString(), receivedUser2Name, receivedFilename).toString());
                            }
                            else {

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                
                                // taille username
                                byte receivedUser2NameLength = receivedData[1];

                                // username
                                bos.write(receivedData, 2, receivedUser2NameLength);
                                String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                                bos.reset();

                                // Taille filename
                                byte receivedFilenameLength = receivedData[1+receivedUser2NameLength+1];

                                // Filename
                                bos.write(receivedData, (1+receivedUser2NameLength+2), receivedFilenameLength);
                                String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());

                                Paths.get(pair.getToSendFilesDir().toString(), receivedUser2Name, receivedFilename).toFile().delete();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

        }).start();
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
      
          new XPair(args[1], Integer.valueOf(args[3]), Integer.valueOf(args[2]), args[0]).launch();
      }
}