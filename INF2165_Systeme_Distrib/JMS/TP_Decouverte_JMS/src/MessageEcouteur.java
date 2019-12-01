import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;

/**
 * Classe permettant de spécifier le comportement lorsque l'application reçoit un message.
 */
public class MessageEcouteur implements MessageListener {

    public void onMessage(Message message) {

        try {

            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;
                System.out.println("Recu : " + text.getText());
            } else if (message != null) {
                System.out.println("Le message reçu n'est pas un message texte.");
            }
        } catch(JMSException je) {
            je.printStackTrace();
        }
    }
}