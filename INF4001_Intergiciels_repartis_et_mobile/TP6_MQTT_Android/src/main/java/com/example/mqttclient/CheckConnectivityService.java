package com.example.mqttclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class CheckConnectivityService extends IntentService {

    public static final String TAG = "CheckConnectivity";
    public static final String BROKER_ADRESS = "broker_adress";
    public static final String BROKER_PORT = "broker_port";
    public static final String TOPIC_NAME = "topic_name";

    public CheckConnectivityService() {

        super("CheckConnectivityService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String brokerAdress = intent.getStringExtra(BROKER_ADRESS);
        String brokerPort = intent.getStringExtra(BROKER_PORT);
        String brokerURI = brokerAdress+":"+brokerPort;
        String topicName = intent.getStringExtra(TOPIC_NAME);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // Non connecté à un réseau
        if(!isConnected) {
            Log.d(TAG, "No internet connexion");
        }
        // Connecté à un réseau
        else {

            MemoryPersistence persistence = new MemoryPersistence();

            try {

                // Connexion au broker
                MqttClient mqttAndroidClient = new MqttClient(brokerURI, "AndroidTest", persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                mqttAndroidClient.connect(connOpts);

                Log.d(TAG, "Connected to broker");

                // Sourcription au topic
                mqttAndroidClient.subscribe(topicName, 0, new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.d(TAG, "New message from " + topic + " ; content="+message.toString());
                        Intent intent = new Intent("MQTT_MessageUpdate");
                        intent.putExtra("Content", message.toString());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                });
            }
            catch(MqttException e) {
                e.printStackTrace();
            }

        }
    }
}
