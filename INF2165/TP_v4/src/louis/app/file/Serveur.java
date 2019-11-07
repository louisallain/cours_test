package louis.app.file;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import louis.app.ubs.MailFile;

/**
 * Cette classe représente un serveur TCP.
 * Ce serveur permet la réception de fichier de quelconque nature provenant de plusieurs clients simultanément.
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
   * Lance un serveur TCP écoutant sur le port précisé en argument.
   * Le serveur attend la connexion de nouveaux clients.
   * Lorsque l'un d'eux se connecte, il recupère le flux de données envoyés par le client pour le réécrire dans un nouveau fichier.
   * Les fichiers reçus sont écrits dans un dossier dont le chemin doit être précisé en paramètre. Si le chemin précisé en paramètre désigne
   * un dossier qui n'existe pas, le serveur le créé.
   * @param portnumber le numéro de port sur lequel le serveur devra écouter
   * @param directoryPath le chemin du dossier dans lequel sauvegarder les fichiers entrants. 
   */
  public void launch(int portnumber, String directoryPath) {

    try {
      ServerSocket socket = new ServerSocket(portnumber);
      System.out.println("Serveur TCP démarré sur le port " + portnumber);
      System.out.println("Dossier de sauvegarde des fichier " + directoryPath);
      Thread t = new Thread(new AccepterClients(socket, Paths.get(directoryPath)));
      t.start();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Lance le serveur sur le port précisé en argument.
   * Attend qu'un client se connecte et que ce client lui transmet un fichier.
   * Le fichier transmi par le client sera sauvegardé dans le chemin en argument.
   * @param args mettre "-h" comme argument pour plus d'infos
   */
  public static void main(String[] args) {

    if(args.length != 2) {
      System.out.println(getUsage());
      return;
    }
    if(args[0].equals("-h")) {
      System.out.println(getUsage());
      return;
    }

    new Serveur().launch(Integer.parseInt(args[0]), args[1]);
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

  /**
   * Lance le thread permettant d'accepter plusieurs clients simultanément.
   * Ouvre un flux de données entre le client et le serveur afin de récupèrer les données puis de les écrire dans un nouveau fichier côté serveur.
   */
  public void run() {

    try {

      while(true) {
        this.clientSocket = this.socketserver.accept();
        System.out.println("Numéro du client accepté : " + nbClient++);

        InputStream clientInputStream = this.clientSocket.getInputStream();

        String filename = this.clientSocket.getInetAddress().toString().concat(String.valueOf(System.currentTimeMillis()));
        Path filePath = Paths.get(this.directory.toString(), filename);

        FileOutputStream fos = new FileOutputStream(filePath.toString());

        // écrit dans le fichier les données reçues
        int count, total = 0;
        byte[] buffer = new byte[4096];
        while((count = clientInputStream.read(buffer)) > 0) {
          fos.write(buffer, 0, count);
          total =+ count;
        }

        clientInputStream.close();
        fos.close();

        System.out.println("Nombre d'octets reçu : " + total);
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
