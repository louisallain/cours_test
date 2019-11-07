package pop;

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
  public static void main(String[] args) {

    if(args.length != 2) {
      System.out.println(getUsage());
    }
    else if(args[0].equals("-h")) {
      System.out.println(getUsage());
    }
    else {

      ServerSocket socket;
      
      try {

        socket = new ServerSocket(Integer.parseInt(args[1]));
        Thread t = new Thread(new AccepterClients(socket, Paths.get(args[0])));
        t.start();
      }
      catch(IOException e) {

        e.printStackTrace();
      }
    }
  }
}

class AccepterClients implements Runnable {

  private ServerSocket socketserver;
  private Socket clientSocket;
  private int nbClient = 1;
  private Path directory;

  public AccepterClients(ServerSocket s, Path directory) {
    this.socketserver = s;
    this.directory = directory;
    this.directory.toFile().mkdirs();
  }

  public void run() {

    try {

      while(true) {

        // accepte la connexion TCP 
        // MAIS n'accepte pas ce client TCP en tant que client du serveur POP3
        // car il faut qu'il s'identifie auprès du serveur POP3
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
