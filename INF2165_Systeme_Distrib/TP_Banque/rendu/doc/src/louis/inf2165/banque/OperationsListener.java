package louis.inf2165.banque;

import javax.jms.*;

/**
 * Classe permettant de spécifier un comportement lorsqu'un nouvelle opérations est ajouté sur la file "operations" de l'application.
 */
public class OperationsListener implements MessageListener {

    /**
     * Le gérant concerné par ce Listener.
     */
    private Gerant gerant;

    /**
     * Créer un objet OperationsListener en fonction du gérant en paramètre.
     * @param gerant le gérant concerné par ce Listener.
     */
    public OperationsListener(Gerant gerant) {
        this.gerant = gerant;
    }

    /**
     * Spécifie le comportement lors de la réception d'un message sur la file "operation" de l'application.
     * @param message le message reçu sur la file "operation"
     */
    public void onMessage(Message message) {

        try {

            if (message instanceof ObjectMessage) {

                ObjectMessage objMsg = (ObjectMessage) message;
                Operation op = (Operation) objMsg.getObject();
                int numCompte = objMsg.getIntProperty("numCompte");
                Compte compte = this.gerant.getCompte(numCompte);
                
                if(compte != null) {
                    System.out.println("Opération à faire : " + op.toString());
                    System.out.println("Sur le compte : " + compte.toString());

                    boolean wasNeg = compte.getSolde() < 0;

                    // applique l'opération reçu sur le compte concerné
                    compte.applyOperation(op);
                    System.out.println("Opération appliquée, nouveau solde : " + String.valueOf(compte.getSolde()));

                    // appel la méthode permettant d'envoyer le nouvel état du compte sur le topic "etatCompte"
                    if(!wasNeg) this.gerant.publishEtatCompte(compte);
                } else {
                    System.out.println("Compte inconnu.");
                }
                
            } else if (message != null) {
                System.out.println("Le message reçu n'est pas un message texte.");
            }
        } catch(JMSException je) {
            je.printStackTrace();
        }
    }
}