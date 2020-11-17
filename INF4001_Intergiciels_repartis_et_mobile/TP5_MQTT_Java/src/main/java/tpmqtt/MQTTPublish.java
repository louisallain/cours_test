package tpmqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Une application permettant de publier périodiquement des données, comme des valeurs de température simulées, sur un topic donné d’un serveur MQTT. 
 * La périodicité d’émission des messages, le topic et l’adresse IP et le port du serveur MQTT doivent indiqués sur la ligne de commande.
 */
public class MQTTPublish {

    /**
     * Donne l'utilisation de la commande.
     * @return l'utilisation de la commande.
     */
    public static String usage() {
        return "usage: MQTTPublish <addressBroker:port> <topic> <periodicity (s)>";
    }

    /**
     * Génère une température aléatoire entre 20°C et 30°C. Utilisée pour les tests.
     * @return une double entre 20 et 30.
     */
    public static double generateRandomTemp() {
        Random rd = new Random(); // creating Random object
        return rd.nextDouble() * 10 + 20;
    }
    public static void main(String args[]) {

        if(args.length != 3) {
            System.out.println("    . " + MQTTPublish.usage());
            return;
        }

        String broker = args[0];
        String topic = args[1];
        int periodicity = Integer.valueOf(args[2]);
        String content;
        int qos = 2; // 2 = exactly once
        String clientId = "MQTTPublish";
        MemoryPersistence persistence = new MemoryPersistence();

        try {

            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("    . Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("    . Connected");

            while(System.in.available() == 0) {

                content = String.valueOf(MQTTPublish.generateRandomTemp());
                System.out.println("    . Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("    . Message published");
                System.out.println("    . Press any key to stop");
                TimeUnit.SECONDS.sleep(periodicity);
            }


            sampleClient.disconnect();
            System.out.println("    . Disconnected");
            System.exit(0);
        } 
        catch(MqttException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}