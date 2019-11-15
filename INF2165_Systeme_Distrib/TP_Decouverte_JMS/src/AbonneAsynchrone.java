import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.jms.*;


/**
 * Abonné asynchrone à un topic de messages.
 */
public class AbonneAsynchrone {

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

        if (args.length != 2) {
            System.out.println("usage: AbonneSynchrone <topic> <subscriptionName>");
            System.exit(1);
        }

        topicName = args[0];
        subscriptionName = args[1];
        System.out.println("Nouvel abonnement, nom : " + subscriptionName);
        try {

            context = new InitialContext();
            factory = (ConnectionFactory) context.lookup(factoryName);
            topic = (Topic) context.lookup(topicName);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            subscriber = session.createDurableSubscriber(topic, subscriptionName);

            // défini le callback lors de la réception de message
            // ce callback interrompra les autres traitements du programme
            subscriber.setMessageListener(new MessageEcouteur());

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
