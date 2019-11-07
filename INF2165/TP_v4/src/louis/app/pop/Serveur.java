package louis.app.pop;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Cette classe représente un serveur POP3.
 * Ce serveur peut recevoir plusieurs clients en même temps.
 */
public class Serveur {

  /**
   * Donne l'utilisation normale de la méthode principale de la classe.
   */
  private static String getUsage() {
    return "Usage : Serveur directorypathname portnumber";
  }

  /**
   * Lance un serveur TCP à l'écoute de nouveaux clients.
   * Ce serveur permet de recevoir des commandes correspondantes au protocol POP3.
   * Ces commandes sont ensuite traitées via la classe CommandHandler.
   * @param directoryPath le dossier où seront sauvegardés toutes les données de chaque client
   * @param portnumber le port sur lequel le serveur écoute
   */
  public void launch(String directoryPath, int portnumber) {
    
    ServerSocket socket;
      
      try {

        socket = new ServerSocket(portnumber);
        Thread t = new Thread(new AccepterClients(socket, Paths.get(directoryPath)));
        t.start();
      }
      catch(IOException e) {

        e.printStackTrace();
      }
  }

  /**
   * Main de la classe.
   * Utilise principalement la méthode launch.
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

    new Serveur().launch(args[0], Integer.parseInt(args[1]));
  }
}

/**
 * Cette classe interne représente le Thread permettant au serveur d'accepter plusieurs clients simultanément.
 */
class AccepterClients implements Runnable {

  private ServerSocket socketserver;
  private Socket clientSocket;
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
   * Ouvre un flux de données entre le client et le serveur et traite ces données grâce à la classe CommandHandler.
   */
  public void run() {

    try {

      while(true) {

        clientSocket = socketserver.accept();
        System.out.println("Nouveau client");
        try(
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ){
          
          String commandReceived, result;
          CommandHandler handler = new CommandHandler(this.directory);
          
          out.println("+OK POP3 ready");
          
          while ((commandReceived = in.readLine()) != null) {
            
            result = handler.getResultOfCommand(commandReceived);
            out.println(result);

            if(commandReceived.matches("quit") || commandReceived.matches("QUIT")) {
              break;
            } 
          }
          out.println("+OK POP3 server signing off");
          clientSocket.close();
        }
      }
    }
    catch(IOException e) {
      e.printStackTrace();
    }
  }
}
