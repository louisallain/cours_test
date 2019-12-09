package echo;

import java.rmi.Naming;

public class EchoClient{
  
  public static void main(String[] args) throws Exception{ 
    
    String host= args.length>=1 ? args[0] : Echo.host;
    Echo server= null;
    try {
      server = (Echo) Naming.lookup("rmi://"+host+":"+Echo.port+"/"+Echo.name);
      System.out.println("[echo client] Echo server found on "+host);
    } catch (Exception e) {
      System.err.println("[echo client] cannot found an Echo server on "+host);
      System.exit(1);
    }
    System.out.println("[echo client] receiving echo from server: "+server.echo("he ho, y a quelqu'un ?"));
  }
}
