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
 * Cette classe représente un client se connectant au serveur de fichier.
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
   * Créer un client qui se connecte à un serveur.
   * Le fichier en argument est transmi au serveur en étant converti en octet.
   * @param args
   */
  public static void main (String[] args) {

    if(args.length != 3) {
      System.out.println(getUsage());
    }
    else if(args[0].equals("-h")) {
      System.out.println(getUsage());
    }
    else {

      String hostname = args[0];
      int port = Integer.parseInt(args[1]);
      String pathname = args[2];

      // Créer le socket

      Path path = Paths.get(pathname);
      File toSendFile = new File(pathname);
      if(toSendFile.length() > Integer.MAX_VALUE) {
        System.out.println("Le fichier est trop volumineux.");
        return;
      }
      byte[] fileBytes = new byte[(int) toSendFile.length()];
      try (
        
        Socket socket = new Socket(hostname, port);
        OutputStream os = socket.getOutputStream();
        FileInputStream fis = new FileInputStream(toSendFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
      ){

        // converti le fichier en tableau d'octets
        bis.read(fileBytes, 0, fileBytes.length);
        System.out.println("Envoi le fichier " + pathname + "(" + fileBytes.length + ")");
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
  }
}
