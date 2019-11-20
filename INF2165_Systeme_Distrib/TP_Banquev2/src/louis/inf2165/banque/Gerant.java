package louis.inf2165.banque;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Set;
import java.util.HashSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.*;

/**
 * Cette classe représente le gérant de l'application.
 * Il s'agit de l’entité conservant les données relatives à chaque compte et appliquant les
 * opérations sur ce compte.
 * Cet objet se connecte évidemment au MOM OpenJMS afin de traiter toutes ces informations.
 */
public class Gerant {
    
    /**
     * Liste des comptes bancaires des clients.
     */
    private Set<Compte> comptes;
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
     * Le Topic "etatCompte" de l'application bancaire.
     */
    private Destination etatCompteTopic;

    /**
     * Créer un un objet Gerant avec une liste de comptes vide.
     * Initialise également la connexion au MOM Open
     */
    public Gerant() {

        this.comptes = new HashSet<Compte>();
        try (InputStream input = new FileInputStream("config.properties")) {

            Properties prop = new Properties();
            prop.load(input);


            this.context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) this.context.lookup(prop.getProperty("louis.inf2165.banque.CONNECTION_FACTORY_NAME"));   
            this.operationsQueue = (Destination) this.context.lookup(prop.getProperty("louis.inf2165.banque.OPERATIONS_QUEUE_NAME"));
            this.etatCompteTopic = (Destination) this.context.lookup(prop.getProperty("louis.inf2165.banque.ACCOUNT_STATE_TOPIC_NAME"));
            this.connexion = factory.createConnection();
            Session session = this.connexion.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer receiver = session.createConsumer(this.operationsQueue);
            receiver.setMessageListener(new OperationsListener(this));
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
     * Créer un objet Gerant avec la liste de comptes en paramètre.
     * @param comptes une liste comptes
     */
    public Gerant(HashSet<Compte> comptes) {

        this();
        this.comptes = comptes;
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
     * Ajoute le compte en paramètre s'il n'est pas déjà dans la liste des comptes du Gerant.
     * Si le compte existe déjà, ne fait rien et donne un message d'erreur.
     * @param compte un compte.
     */
    public void addCompte(Compte compte) {
        this.comptes.add(compte);
    }

    /**
     * Supprime le compte en paramètre de la liste des comptes s'il existe.
     * S'il n'existe pas, alors donne un message d'erreur et ne fait rien.
     * @param compte le compte à être supprimer.
     */
    public void removeCompte(Compte compte) {
        this.comptes.remove(compte);
    }

    /**
     * Donne le compte correspondant au numéro de compte en paramètre s'il existe.
     * Si ce numéro de compte ne correspond à aucun compte, ne fait rien et donne un message d'erreur.
     * @return le compte correspondant au numéro de compte ou null si rien ne correspond.
     */
    public Compte getCompte(int numCompte) {
        
        Compte tmpC = null;

        for(Compte c : this.comptes) {
            if(c.getNumCompte() == numCompte) tmpC = c;
        }
        if(tmpC == null) System.out.println("Aucun compte ne correspond à ce numéro de compte.");
        
        return tmpC;
    }

    /**
     * Publie sur le topic "etatCompte" l'état d'un compte.
     * Ce n'est pas l'objet Compte qui est publié mais seulement les informations représentant ce compte.
     * Ces informations sont transmises via un MapMessage dont le corps contient une table du genre : 
     *      [
     *       {"numCompte", numero_du_compte},
     *       {"solde", solde_du_compte}
     *      ]
     * On ajoute également des propriétés permettant de filter les messages du genre : 
     *      [
     *       {"dateCreation", date_creation_compte},
     *       {"solde", solde_compte}
     *      ]
     * Ainsi toutes les informations permettant de représenter un compte sont précisées. L'historique des opérations du comptes 
     * ne sont pas transmises pour des raisons de performances.
     * @param compte le compte dont les informations doivent être publiées sur le topic "etatCompte".
     */
    public void publishEtatCompte(Compte compte) {
        
        try {
            
            Session session = this.connexion.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(this.etatCompteTopic);
            MapMessage message = session.createMapMessage();
            message.setLongProperty("dateCreation", compte.getDate().getTime());
            message.setIntProperty("solde", compte.getSolde());
            message.setInt("numCompte", compte.getNumCompte());
            message.setInt("solde", compte.getSolde());
            sender.send(message);
            System.out.println("Envoye nouvel état du compte : " + compte.toString());
        } catch (JMSException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Donne l'utilisation normale de la méthode principale de la classe Gerant.
     * @return l'utilisation normale de la méthode principale de la classe Gerant.
     */
    public static String usage() {
        return "Usage : Gerant";
    }

    /**
     * Méthode princiaple de la classe Gerant.
     * Lance un gérant traitant les activités de l'applicatioin bancaire.
     * Le gerant est initialisé avec la liste des clients de l'application directement dans la méthode.
     * @param args inutilisé
     */
    public static void main(String[] args) {
        
        Gerant gerant = new Gerant();
        gerant.addCompte(new Compte(1, new Client("louis")));
        gerant.addCompte(new Compte(2, new Client("louis")));

        gerant.addCompte(new Compte(10, new Client("agathe")));
        gerant.addCompte(new Compte(20, new Client("agathe")));
    }
}