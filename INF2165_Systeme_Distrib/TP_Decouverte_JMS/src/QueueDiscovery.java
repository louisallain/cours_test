import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Cette classe permet de tester la communication en mode point à point de JMS.
 * @author e1602246
 */
public class QueueDiscovery {

	/**
	 * Nom de la "factory" inscrite dans les paramètres.
	 */
	private final static String FACTORY_NAME = "ConnectionFactory";
	/**
	 * Nom de la file précisée dans le TP.
	 */
	private final static String DESTINATION_NAME = "queue1";

	/**
	 * Contexte initial de l'application.	
	 */
	private Context context;
	/**
	 * Fabrique de connexion de l'application.
	 */
	private ConnectionFactory factory;
	/**
	 * Connexion au serveur OpenJMS.
	 */
	private Connection connection;
	/**
	 * Objet Destination, pouvant être une file ou un "topic".
	 */
	private Destination destination;
	/**
	 * Objet Session.
	 */
	private Session session;
	/**
	 * Objet MessageProducer représentant le producteur des messages.
	 */
	private MessageProducer producer;
	/**
	 * Objet MessageConsumer représentant le consommateur des messages.
	 */
	private MessageConsumer consumer;

	/**
	 * Initialise le contexte, la connexion et la sesssion nécessaires 
	 * au déroulement du programme.
	 */
	public QueueDiscovery() {
		
		try {

			// Initialise l'ensemble des paramètres nécessaires à l'application.
			this.context = new InitialContext();
			this.factory = (ConnectionFactory) this.context.lookup(QueueDiscovery.FACTORY_NAME);
			this.destination = (Queue) this.context.lookup(QueueDiscovery.DESTINATION_NAME);
			this.connection = this.factory.createConnection();
			this.producer = this.session.createProducer(this.destination);
			this.consumer = this.session.createConsumer(this.destination);

		} catch(JMSException je) {
			je.printStackTrace();
		} catch(NamingException ne) { 
			ne.printStackTrace();
		}
	}

	/**
	 * Donne l'utilisation normal du programme.
	 */
	public static String usage() {
		return "Usage : QueueDiscovery nbMessages";
	}
	
    /**
	 * Méthode principale de l'application.
	 * Envoi des messages aléatoires (entier aléatoires) sur la file "file1".
	 * @param args args[0] : nombre de messages à émettre.
	 */
	public static void main(String[] args) {
	
		if(args.length != 1) {
			System.out.println(QueueDiscovery.usage());
			System.exit(1);
		}

		QueueDiscovery queueDiscovery = new QueueDiscovery();


		try {
			// Démarre la connexion
			queueDiscovery.connection.start();

			int nbMessages = Integer.parseInt(args[0]);

			// Envoi les messages
			for(int i = 0; i < nbMessages; i++) {

				TextMessage message = queueDiscovery.session.createTextMessage();
				message.setText("Message n° : " + i);
				queueDiscovery.producer.send(message);
				System.out.println("Le message suivant a été envoyé : " + message.getText());
			}

			// // Réception des messages
			// for(int i = 0; i < nbMessages; i++) {

			// 	Message message = queueDiscovery.consumer.receive();
			// 	if (message instanceof TextMessage) {
			// 		TextMessage text = (TextMessage) message;
			// 		System.out.println("Le message suivant a été reçu : " + text.getText());
			// 	} else if (message != null) {
			// 		System.out.println("Le message reçu n'est pas un message de texte.");
			// 	}
			// }

		} catch(JMSException je) {
			je.printStackTrace();
		} finally {

			// Réinitalise le contexte			
			if (queueDiscovery.context != null) {
				try {
					queueDiscovery.context.close();
				} catch (NamingException exception) {
					exception.printStackTrace();
				}
			}
			// Réinitalise la connexion	
			if (queueDiscovery.connection != null) {
				try {
					queueDiscovery.connection.close();
				} catch (JMSException exception) {
					exception.printStackTrace();
				}
			}
		}
	}
}
