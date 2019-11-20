package louis.inf2165.banque;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Set;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.*;

/**
 * Cette classe regroupe les départements d'investissements et des découverts.
 */
public class Departements {

    /**
     * La connexion au MOM OpenJMS.
     */
    private Connection connexion;
    /**
     * Le contexte du MOM OpenJMS.
     */
    private Context context;
    /**
     * Le Topic "etatCompte" de l'application bancaire.
     */
    private Topic etatCompteTopic;
    /**
     * L'objet représente l'abonné au topic "etatCompte" utile pour le département des investissements.
     */
    private TopicSubscriber abonneInvestissements;
    /**
     *  L'objet représente l'abonné au topic "etatCompte" utile pour le département des découverts.
     */
    private TopicSubscriber abonneDecouverts;

    /**
     * Créer un objet Departements.
     * Initialise la connexion à OpenJMS ainsi que les différents abonnés au topic "etatCompte" (investissements et découverts).
     */
    public Departements() {

        try (InputStream input = new FileInputStream("config.properties")) {

            Properties prop = new Properties();
            prop.load(input);


            this.context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) this.context.lookup(prop.getProperty("louis.inf2165.banque.CONNECTION_FACTORY_NAME"));
            this.etatCompteTopic = (Topic) this.context.lookup(prop.getProperty("louis.inf2165.banque.ACCOUNT_STATE_TOPIC_NAME"));
            this.connexion = factory.createConnection();
            Session sessionDecouverts = this.connexion.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Session sessionInvestissements = this.connexion.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            long dateLimite = cal.getTime().getTime();
            this.abonneInvestissements = sessionInvestissements.createDurableSubscriber(this.etatCompteTopic, prop.getProperty("louis.inf2165.banque.INVESTMENTS_SUBSCRIPTION_NAME"), "solde >= 10000 AND dateCreation > " + dateLimite, false);
            this.abonneDecouverts = sessionDecouverts.createDurableSubscriber(this.etatCompteTopic, prop.getProperty("louis.inf2165.banque.OVERDRAFT_SUBSCRIPTION_NAME"), "solde < 0", false);

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
     * Cette méthode créer un Thread permettant de traiter la réception de messages, filtrés selon les critère du départements des découverts,
     * depuis le Topic "etatCompte".
     * Le comportement lors de la réception d'un message est le suivant : 
     * Pour chaque compte, il étudie l’évolution du solde et lorsque celui devient négatif, il fait retirer par le gérant une somme forfaitaire
     * (e.g. 10 e) sur le compte.
     */
    public void receiveForDecouverts() {

        Thread dT = new Thread() {
            public void run() {
                try {
                    Message message = abonneDecouverts.receive();
                    if (message instanceof MapMessage) {
                        MapMessage text = (MapMessage) message;
                        System.out.println("Recu decouvert");
                    } else if (message != null) {
                        System.out.println("Le message reçu n'est pas un MapMessage.");
                    }
                } catch(JMSException e) {
                    e.printStackTrace();
                }
            }
        };
        dT.start();
    }

    /**
     * Cette méthode créer un Thread permettant de traiter la réception de messages, filtrés selon les critère du départements des investissements,
     * depuis le Topic "etatCompte".
     * Le comportement du Thread est le suivant : 
     * Surveille l’évolution du solde des comptes. Seuls les nouveaux comptes, ouverts dans
     * l’année, l’intéressent (on assimilera la date d’ouverture d’un compte à la date de la première opération). Son objectif
     * est de détecter que le solde dépasse un certain seuil (e.g. 10 000 e) à partir duquel il fera une proposition de placement financier à
     * l’usager (par un moyen de type courrier électronique qui ne sera pas géré par votre application).
     */
    public void receiveForInvestissements() {

        Thread dI = new Thread() {
            public void run() {
                try {
                    Message message = abonneInvestissements.receive();
                    if (message instanceof MapMessage) {
                        MapMessage text = (MapMessage) message;
                        System.out.println("Recu investissement");
                    } else if (message != null) {
                        System.out.println("Le message reçu n'est pas un MapMessage.");
                    }
                } catch(JMSException e) {
                    e.printStackTrace();
                }
            }
        };
        dI.start();
    }

    /**
     * Méthode princiaple de la classe Gerant.
     * Lance un gérant traitant les activités de l'applicatioin bancaire.
     * Le gerant est initialisé avec la liste des clients de l'application directement dans la méthode.
     * @param args inutilisé
     */
    public static void main(String[] args) {
        
        Departements dept = new Departements();
        dept.receiveForDecouverts();
        dept.receiveForInvestissements();
    }
}