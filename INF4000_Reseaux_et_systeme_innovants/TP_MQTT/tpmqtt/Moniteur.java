package tpmqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class Moniteur {

    private String topicName;
    private static String broker = "tcp://mini.arsaniit.com:1883";

    public Moniteur(String nomPiece) {
        this.topicName = "/e1602246/maison/" + nomPiece;
    }

    /**
     * Démarre le moniteur.
     */
    public void start() {

        String clientId = "moniteur" + this.topicName;
        MemoryPersistence persistence = new MemoryPersistence();
        int qos = 0;

        try {

            MqttClient sampleClient = new MqttClient(Moniteur.broker, clientId, persistence);
            sampleClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable arg0) { }

                @Override
                public void deliveryComplete(IMqttDeliveryToken arg0) { }

                @Override
                public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
                    
                    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                    Date date = new Date(System.currentTimeMillis());
                    System.out.println(String.format("[%s] %s", topicName, "Presence detected at " + formatter.format(date)));
                }
                
            });
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("    . Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("    . Connected");
            sampleClient.subscribe(this.topicName, qos);
            System.out.println("    . Subscribed");



            System.out.println("    . Press any key to stop");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try { br.readLine(); } catch (IOException e) { }

            // Déconnexion 
            sampleClient.disconnect();
            System.out.println("    . Disconnected");
            System.exit(0);
        } 
        catch(MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Moniteur m = new Moniteur("cuisine");
        m.start();
    }
}