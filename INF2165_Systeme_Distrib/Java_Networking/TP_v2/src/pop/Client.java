
package pop;

import java.net.Socket;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Cette classe représente le client d'un serveur POP3.
 * Il est capable de communiquer (envoyer des commandes / recevoir des répones) avec le serveur.
 */
public class Client {

  /**
   * Donne l'utilisation normale de la méthode principale de la classe.
   */
  private static String getUsage() {
    return "Usage : Client hostname portnumber";
  }

  public static void main (String[] args) {

    if(args.length != 2) {
      System.out.println(getUsage());
    }
    else if(args[0].equals("-h")) {
      System.out.println(getUsage());
    }
    else {

      String hostname = args[0];
      int port = Integer.parseInt(args[1]);
      String command = "";
      String answer = "";
      // Créer le socket
      try (
        Socket socket = new Socket(hostname, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader cmdIn = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      ){
        while((answer = in.readLine()) != null) {
          System.out.println(answer);
          
          
          if((command = cmdIn.readLine()) != null) {
            out.println(command);
           
          }
        }
        out.close();

        
        socket.close();
      }
      catch(Exception e) {
        System.err.println(e);
      }
    }
  }
}
