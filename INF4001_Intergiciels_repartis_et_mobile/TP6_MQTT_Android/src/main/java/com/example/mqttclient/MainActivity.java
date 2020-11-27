package com.example.mqttclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText brokerAdressEditText, brokerPortEditText, topicNameEditText;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AppCompatActivity t = this;

        // Récupération des données de configuration
        brokerAdressEditText = (EditText) findViewById(R.id.ipServerMqttEditText);
        brokerPortEditText = (EditText) findViewById(R.id.portServerMqttEditText);
        topicNameEditText = (EditText) findViewById(R.id.topicMqttEditText);

        // Bouton de connexion
        connectButton = (Button) findViewById(R.id.saveConfigButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"Config sauvegardée",Toast.LENGTH_LONG).show();
                Intent configIntent = new Intent(t, CheckConnectivityService.class);
                configIntent.putExtra(CheckConnectivityService.BROKER_ADRESS, brokerAdressEditText.getText().toString());
                configIntent.putExtra(CheckConnectivityService.BROKER_PORT, brokerPortEditText.getText().toString());
                configIntent.putExtra(CheckConnectivityService.TOPIC_NAME, topicNameEditText.getText().toString());
                startService(configIntent);
            }
        });

        // Envoi des données au service vérifiant la connectivité réseau + souscription au sujet
        Intent configIntent = new Intent(this, CheckConnectivityService.class);
        configIntent.putExtra(CheckConnectivityService.BROKER_ADRESS, brokerAdressEditText.getText().toString());
        configIntent.putExtra(CheckConnectivityService.BROKER_PORT, brokerPortEditText.getText().toString());
        configIntent.putExtra(CheckConnectivityService.TOPIC_NAME, topicNameEditText.getText().toString());
        startService(configIntent);

        // Ajoute un listener sur l'état de la connexion
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive", "changed");
                Intent configIntent = new Intent(t, CheckConnectivityService.class);
                configIntent.putExtra(CheckConnectivityService.BROKER_ADRESS, brokerAdressEditText.getText().toString());
                configIntent.putExtra(CheckConnectivityService.BROKER_PORT, brokerPortEditText.getText().toString());
                configIntent.putExtra(CheckConnectivityService.TOPIC_NAME, topicNameEditText.getText().toString());
                startService(configIntent);
            }
        }, filter);

        // Affichage de la liste
        final ArrayList<MsgListData> msgList = new ArrayList<MsgListData>();
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.msgRecyclerView);
        final MsgListAdapter msgListAdapter = new MsgListAdapter(msgList);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(msgListAdapter);
        msgList.add(new MsgListData("hello"));

        // Nouveau message MQTT
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MAIN ACTIVITY", intent.getStringExtra("Content"));
                msgList.add(new MsgListData(intent.getStringExtra("Content")));
                msgListAdapter.notifyDataSetChanged();
            }
        }, new IntentFilter("MQTT_MessageUpdate"));
    }
}