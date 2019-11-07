package louis.app.file;

import java.net.Socket;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.OutputStream;

/**
 * Cette classe représente un client TCP.
 * Ce client permet l'envoi d'un fichier de quelconque nature.
 */
public class Client {

   /**
   * Donne l'utilisation correcte de la méthode principale.
   * @return une chaine contenant les informations sur la bonne utilisatin de la méthode principale.
   */
  private static String getUsage() {
    return "Usage : Client hostname portnumber filename";
  }

  /**
   * Lance le client TCP permettant d'envoyer un fichier vers un serveur TCP.
   * @param hostName le nom de l'hôte du serveur TCP à qui on souhaite envoyer un fichier
   * @param portnumber le port sur lequel le serveur TCP écoute
   * @param filename le nom du fichier à envoyer au serveur TCP
   */
  public void launch(String hostname, int portnumber, String filename) {

    File toSendFile = new File(filename);
    if(toSendFile.length() > Integer.MAX_VALUE) {
      System.out.println("Le fichier est trop volumineux.");
      return;
    }
    byte[] fileBytes = new byte[(int) toSendFile.length()];
    try (

      Socket socket = new Socket(hostname, portnumber);
      OutputStream os = socket.getOutputStream();
      FileInputStream fis = new FileInputStream(toSendFile);
      BufferedInputStream bis = new BufferedInputStream(fis);
    ){

      // converti le fichier en tableau d'octets
      bis.read(fileBytes, 0, fileBytes.length);
      System.out.println("Envoi le fichier " + filename + "(" + fileBytes.length + ")");
      os.write(fileBytes, 0, fileBytes.length);
      System.out.println("Terminé");
      os.flush();
      os.close();
      socket.close();
      bis.close();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Main du client TCP.
   * Utilise principalement la méthode launch.
   * @param args mettre "-h" comme argument pour plus d'infos
   */
  public static void main (String[] args) {

    if(args.length != 3) {
      System.out.println(getUsage());
      return;
    }
    if(args[0].equals("-h")) {
      System.out.println(getUsage());
      return;
    }

    new Client().launch(args[0], Integer.parseInt(args[1]), args[2]);
  }
}
