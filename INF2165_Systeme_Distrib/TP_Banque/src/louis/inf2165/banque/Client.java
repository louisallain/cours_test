package louis.inf2165.banque;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.*;

/**
 * Cette classe représente un client de l'application.
 * cette entité effectue des opérations sur un compte (il représente l’usager au guichet) en envoyant
 * des messages au gérant. Ces messages serviront à faire des dépots et des retraits. Toutes les opérations seront datées.
 * On pourra considérer plusieurs instances de clients correspondant chacune à un usager différent.
 */
public class Client {   

    /**
     * Le nom du client (ie : son identifiant également)
     */
    private String nomClient;
    /**
     * La connexion au MOM OpenJMS.
     */
    private Connection connexion;

    /**
     * Le contexte du MOM OpenJMS.
     */
    private Context context;
    /**
     * La file "operations" de l'application bancaire.
     */
    private Destination operationsQueue;

    /**
     * Créer un nouvel objet Client.
     * Initialise également la connexion au MOM OpenJMS.
     * @param nomClient le nom du client (ie : son identifiant également)
     * @param gerant le gerant du client
     */
    public Client(String nomClient) {
        
        this.nomClient = nomClient;

        try (InputStream input = new FileInputStream("config.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            this.context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) this.context.lookup(prop.getProperty("louis.inf2165.banque.CONNECTION_FACTORY_NAME"));   
            this.operationsQueue = (Destination) this.context.lookup(prop.getProperty("louis.inf2165.banque.OPERATIONS_QUEUE_NAME"));
            this.connexion = factory.createConnection();
            this.connexion.start();
        } catch (JMSException exception) {
            exception.printStackTrace();
        } catch (NamingException exception) {
            exception.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Donne le nom du client.
     * @return le nom du client.
     */
    public String getNomClient() {
        return this.nomClient;
    }

    /**
     * Modifie le nom du client.
     * @param nomClient un nouveau nom pour le clien.
     */
    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    /**
     * Représente un objet Client sous la forme d'une chaine de caractère.
     * @return une représentation textuelle d'un objet Client.
     */
    @Override
    public String toString() {
        return "{" +
            " nomClient='" + getNomClient() + "'" +
            "}";
    }

    /**
     * Envoie une opération (push) sur la file "operations" afin qu'un gérant puisse la traiter.
     * Il est nécessaire de préciser le numéro de compte sur lequel on souhaite appliquer une opération car
     * un client peut avoir plusieurs compte. Ce sera au gérant de gérer le fait de savoir si le numéro de compte donné
     * appartient bien au client invoquant cette méthode.
     * Cette méthode envoie donc un message de type ObjectMessage sur la file "operations" contenant l'objet "op" en tant que 
     * corps de message et des propriétés du genre : 
     *      [
     *       {"idClient", id_du_client_courant},
     *       {"numCompte", num_compte_en_parametre}
     *      ]
     * @param op l'opération que l'on souhaite faire appliquer par le gérant sur le compte en paramètre.
     */
    public void envoyerOperation(Operation op, int numCompte) {
        
        try {
            
            Session session = this.connexion.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(this.operationsQueue);
            ObjectMessage message = session.createObjectMessage(op);
            message.setStringProperty("idClient", this.nomClient);
            message.setIntProperty("numCompte", numCompte);
            sender.send(message);
            System.out.println("Envoye : " + op.toString());
        } catch (JMSException exception) {
            exception.printStackTrace();
        } finally {
            this.closeAll();
        }
    }

    /**
     * Cette méthode ferme toutes les connexions avec JMS.
     */
    public void closeAll() {
        try {
            this.context.close();
            this.connexion.close();
        } catch (JMSException exception) {
            exception.printStackTrace();
        } catch (NamingException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Donne l'utilisation normale de la méthode principale de la classe Client.
     * @return l'utilisation normale de la méthode principale de la classe Client.
     */
    public static String usage() {
        return "Usage : Client <nomClient> <numCompte> <montant> \n Si le montant est inférieur à 0, on considère que c'est un retrait sinon c'est un dépôt";
    }

    /**
     * Méthode principale de la classe Client.
     * Permet d'envoyer une opération à faire sur un compte.
     * Le client envoyant l'opération doit déjà exister dans l'application (voir la méthode principale du Gerant).
     * @param args args[0] : nomClient (String) , args[1] : numCompte (int) , args[2] : montant (int ; si < 0 on considère que c'est un retrait sinon c'est un dépôt)
     */
    public static void main(String[] args) {

        if(args.length != 3) {
            System.out.println(Client.usage());
            System.exit(1);
        }

        Client client = new Client(args[0]);
        int numCompte = Integer.valueOf(args[1]);
        int montant = Integer.valueOf(args[2]);
        TypeOperation typeOp = montant < 0 ? TypeOperation.RETRAIT : TypeOperation.DEPOT;
        
        client.envoyerOperation(new Operation(typeOp, montant), numCompte);
    }
}