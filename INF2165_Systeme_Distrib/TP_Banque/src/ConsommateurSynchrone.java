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
 * Consommateur synchrone de messages.
 */
public class ConsommateurSynchrone {

    /**
     * Programme principal.
     * @param args args[0] : NOT NULL = "Destination" ; args[1] = "count"
     */
    public static void main(String[] args) {

        Context context = null;
        ConnectionFactory factory = null;
        Connection connection = null;
        String factoryName = "CF";
        String destName = null;
        Destination dest = null;
        int count = 1;
        Session session = null;
        MessageConsumer receiver = null;

        if (args.length < 1 || args.length > 2) {
            System.out.println("usage: ConsommateurSynchrone <destination> [count]");
            System.exit(1);
        }

        destName = args[0];
        if (args.length == 2) {
            count = Integer.parseInt(args[1]);
        }

        try {

            context = new InitialContext();
            factory = (ConnectionFactory) context.lookup(factoryName);
            dest = (Destination) context.lookup(destName);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            receiver = session.createConsumer(dest);

            // démarre la connexion afin d'activer la réception de messages
            connection.start();

            for (int i = 0; i < count; ++i) {
                Message message = receiver.receive();
                if (message instanceof TextMessage) {
                    TextMessage text = (TextMessage) message;
                    System.out.println("Recu : " + text.getText());
                } else if (message != null) {
                    System.out.println("Le message reçu n'est pas un message texte.");
                }
            }
        } catch (JMSException exception) {
            exception.printStackTrace();
        } catch (NamingException exception) {
            exception.printStackTrace();
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
