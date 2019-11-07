import java.io.*;
import java.net.*;
import java.nio.file.*;
import fr.ubs.io.MailFile;

/**
 * Cette classe représente un serveur de fichier.
 */
public class Serveur {
  
  /**
   * Donne l'utilisation correcte de la méthode principale.
   * @return une chaine contenant les informations sur la bonne utilisatin de la méthode principale.
   */
  private static String getUsage() {
    return "Usage : Serveur portnumber directorypathname";
  }
  /**
   * Lance le serveur sur le port précisé en argument.
   * Attend qu'un client se connecte et que ce client lui transmet un fichier.
   * Le fichier transmi par le client sera sauvegardé dans le chemin en argument.
   * @param args
   */
  public static void main(String[] args) {

    if(args.length != 2) {
      System.out.println(getUsage());
    }
    else if(args[0].equals("-h")) {
      System.out.println(getUsage());
    }
    else {
   
      try {
        ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]));
        Thread t = new Thread(new AccepterClients(socket, Paths.get(args[1])));
        t.start();
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }
  }
}

/**
 * Cette classe interne représente le Thread permettant au serveur d'accepter plusieurs clients simultanément.
 */
class AccepterClients implements Runnable {

  private ServerSocket socketserver;
  private Socket clientSocket;
  private int nbClient = 1;
  private Path directory;

  /**
   * Initialise un objet AccepterClients.
   * @param s le socket du serveur
   * @param directory le chemin du dossier dans lequel seront sauvegardés les fichiers.
   */
  public AccepterClients(ServerSocket s, Path directory) {
    this.socketserver = s;
    this.directory = directory;
    this.directory.toFile().mkdirs();
  }

  public void run() {

    try {

      while(true) {
        this.clientSocket = this.socketserver.accept();
        System.out.println("Numéro du client accepté : " + nbClient++);

        InputStream clientInputStream = this.clientSocket.getInputStream();

        String filename = this.clientSocket.getInetAddress().toString().concat(String.valueOf(System.currentTimeMillis()));
        Path filePath = Paths.get(this.directory.toString(), filename);

        FileOutputStream fos = new FileOutputStream(filePath.toString());
        byte[] receivedBytes = new byte[clientInputStream.available()];
        clientInputStream.read(receivedBytes, 0, receivedBytes.length);
        fos.write(receivedBytes, 0, receivedBytes.length);

        clientInputStream.close();
        fos.close();
        
        System.out.println("Nombre d'octets reçu : " + receivedBytes.length);
        this.clientSocket.close();

        MailFile mailFile = new MailFile(filePath.toFile());
        String messageId = mailFile.getMessageId();
        if (messageId != null) {
          String from = mailFile.getFrom();
          mailFile.updateFilename();
          String tmpFilename = mailFile.getFile().getName();
          System.out.println("MessageId: " + messageId);
          System.out.println("From     : " + from);
          System.out.println("Fichier renommé en tant que " + tmpFilename);
        } 
        else {
          System.out.println("Ce n'est pas un fichier email");
        }
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }
}
