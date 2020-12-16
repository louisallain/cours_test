package tpmqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import saim.mqtt.SimPir;

/**
 * Application simulant l'envoi de donnée d'un capteur de présence.
 * Un programme capteur signale la détection d'un mouvement en publiant un chaine de caractère "detected" à chaque fois qu'il détecte un mouvement.
 * Il émet ce message toutes les 10 secondes tant que le mouvement est détecté.
 */
public class Capteur {

    private String topicName;
    private SimPir capteur; // simulation d'un capteur de mouvement
    private static String broker = "tcp://mini.arsaniit.com:1883";

    /**
     * Nouveau capteur pour une pièce donnée.
     * Le topic dans lequel écrira le capteur est /e1602246/maison/{nomPiece}
     */
    public Capteur(String nomPiece) {
        this.topicName = "/e1602246/maison/" + nomPiece;
        List<Integer> periods = new ArrayList<>();
        periods.add(Integer.valueOf(5));
        periods.add(Integer.valueOf(30));
        periods.add(Integer.valueOf(10));
        periods.add(Integer.valueOf(40));
        this.capteur = new SimPir(periods);
    }

    /**
     * Démarre le capteur et l'envoie de données.
     */
    public void start() {

        // Démarre la simulation du capteur
        this.capteur.start();

        String content;
        int qos = 0;
        String clientId = "capteur"+this.topicName;
        MemoryPersistence persistence = new MemoryPersistence();

        try {

            MqttClient sampleClient = new MqttClient(Capteur.broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("    . Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("    . Connected");

            System.out.println("    . Press any key to stop");

            while(System.in.available() == 0) { // boucle infinie tant qu'il n'y pas de saisie clavier

                // Un mouvement est détecté
                if(this.capteur.getState() == true) {
                    content = "detected";
                    System.out.println("    . Publishing message: " + content);
                    MqttMessage message = new MqttMessage(content.getBytes());
                    message.setQos(qos);
                    sampleClient.publish(this.topicName, message);
                    System.out.println("    . Message published");
                    TimeUnit.SECONDS.sleep(10); // attend 10 secondes avant une autre prise de mesure
                }
            }

            // Déconnexion et arrêt de la simulation du capteur
            this.capteur.stop();
            sampleClient.disconnect();
            System.out.println("    . Disconnected");
            System.exit(0);
        } 
        catch(MqttException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Capteur c = new Capteur("cuisine");
        c.start();
    }
}