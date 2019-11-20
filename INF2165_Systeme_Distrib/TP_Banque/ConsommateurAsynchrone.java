import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Destination;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;


/**
 * Consommateur asynchrone de messages.
 */
public class ConsommateurAsynchrone {

    /**
     * Programme principal.
     * @param args args[0] : NOT NULL = "Destination"
     */
    public static void main(String[] args) {

        Context context = null;
        ConnectionFactory factory = null;
        Connection connection = null;
        String factoryName = "ConnectionFactory";
        String destName = null;
        Destination dest = null;
        Session session = null;
        MessageConsumer receiver = null;

        if (args.length != 1) {
            System.out.println("usage: ConsommateurAsynchrone <destination>");
            System.exit(1);
        }

        destName = args[0];

        try {

            context = new InitialContext();
            factory = (ConnectionFactory) context.lookup(factoryName);
            dest = (Destination) context.lookup(destName);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiver = session.createConsumer(dest);

            // défini le callback lors de la réception de message
            // ce callback interrompra les autres traitements du programme
            receiver.setMessageListener(new MessageEcouteur());

            // démarre la connexion afin d'activer la réception de messages
            connection.start();

            // effectue un autre traitement en attendant la réception d'un message
            while(true) {
                System.out.println(".");
                Thread.sleep(1000);
            }

        } catch (JMSException exception) {
            exception.printStackTrace();
        } catch (NamingException exception) {
            exception.printStackTrace();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        } finally {
            // ferme le "context"
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException exception) {
                    exception.printStackTrace();
                }
            }

            // ferme la "connection"
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
