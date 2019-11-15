import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.jms.*;


/**
 * Abonné synchrone à un topic de messages.
 */
public class AbonneSynchrone {

    /**
     * Programme principal.
     * @param args args[0] : NOT NULL = "Destination" ; args[0] : NOT NULL = "subscriptionName"; args[2] = "count"
     */
    public static void main(String[] args) {

        Context context = null;
        ConnectionFactory factory = null;
        Connection connection = null;
        String factoryName = "ConnectionFactory";
        String topicName = null;
        Topic topic = null;
        int count = 1;
        Session session = null;
        TopicSubscriber subscriber = null;
        String subscriptionName = null;

        if (args.length < 1 || args.length > 3) {
            System.out.println("usage: AbonneSynchrone <topic> <subscriptionName> [count]");
            System.exit(1);
        }

        topicName = args[0];
        subscriptionName = args[1];
        if (args.length == 3) {
            count = Integer.parseInt(args[2]);
        }
        System.out.println("Nouvel abonnement, nom : " + subscriptionName);
        try {

            context = new InitialContext();
            factory = (ConnectionFactory) context.lookup(factoryName);
            topic = (Topic) context.lookup(topicName);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            subscriber = session.createDurableSubscriber(topic, subscriptionName);

            // démarre la connexion afin d'activer la réception de messages
            connection.start();

            for (int i = 0; i < count; ++i) {
                Message message = subscriber.receive();
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
