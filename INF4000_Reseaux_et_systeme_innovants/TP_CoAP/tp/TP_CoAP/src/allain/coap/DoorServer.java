package allain.coap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;

public class DoorServer {

    /**
     * Fermée = false, ouverte = true
     */
    static boolean porteOuverte = false;

    static int nbPersCuisine = 0;
    static int nbPersSalon = 0;
    static int nbPersSejour = 0;

    static int port = 5684;

    /**
     * Verifie la porte doit être ouverte ou fermée.
     */
    public static void checkPorte() {
        if(nbPersCuisine == 0 && nbPersSalon == 0 && nbPersSejour == 0) {
            porteOuverte = false;
        }
        else {
            porteOuverte = true;
        }
    }
    
    public static void main(String args[]) {

        // Créer le serveur CoAP dans un thread pour permettre aux handler des observers de s'exécuter
        Runnable runnable = () -> { 
            // binds on UDP port 5684
            CoapServer server = new CoapServer(DoorServer.port);
            server.add(new PorteResource());
            server.start();
            System.out.println("Server started");
        };
        Thread thread = new Thread(runnable);
        thread.start();

        ////////////////////////////////////////////////////////////////////////

        // Définition des observer
        CoapClient client = new CoapClient("coap://localhost:" + Server.port + "/maison/presence:"+Server.CUISINE);
        CoapObserveRelation r1 = client.observe(new CoapHandler(){
            @Override public void onLoad(CoapResponse response) {
                DoorServer.nbPersCuisine = Integer.parseInt(response.getResponseText().replaceAll("\\D+",""));
                DoorServer.checkPorte();
                System.out.println("Porte ouverte :" + DoorServer.porteOuverte);
            }
            
            @Override public void onError() {
                System.err.println("OBSERVING FAILED");
            }
        });
        client = new CoapClient("coap://localhost:" + Server.port + "/maison/presence:"+Server.SALON);
        CoapObserveRelation r2 = client.observe(new CoapHandler(){
            @Override public void onLoad(CoapResponse response) {
                DoorServer.nbPersSalon = Integer.parseInt(response.getResponseText().replaceAll("\\D+",""));
                DoorServer.checkPorte();
                System.out.println("Porte ouverte :" + DoorServer.porteOuverte);
            }
            
            @Override public void onError() {
                System.err.println("OBSERVING FAILED");
            }
        });
        client = new CoapClient("coap://localhost:" + Server.port + "/maison/presence:"+Server.SEJOUR);
        CoapObserveRelation r3 = client.observe(new CoapHandler(){
            @Override public void onLoad(CoapResponse response) {
                DoorServer.nbPersSejour =  Integer.parseInt(response.getResponseText().replaceAll("\\D+",""));
                DoorServer.checkPorte();
                System.out.println("Porte ouverte :" + DoorServer.porteOuverte);
            }
            
            @Override public void onError() {
                System.err.println("OBSERVING FAILED");
            }
        });

        // Attend un entrée clavier pour arrêter l'observation
        System.out.println("Appuyer sur une touche pour arreter le programme.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try { br.readLine(); } catch (IOException e) { }

        client.shutdown();
        r1.proactiveCancel(); // désactive la relation d'observer
        r2.proactiveCancel(); // désactive la relation d'observer
        r3.proactiveCancel(); // désactive la relation d'observer

        System.exit(0);
    }


    public static class PorteResource extends CoapResource {

        public PorteResource() {
            super("porte");
            getAttributes().setTitle("Resource porte");
        }

        /**
         * Méthode permettant à un client de savoir si la porte est ouverte ou non.
         */
        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond("{porte ouverte=" + DoorServer.porteOuverte + "}");
        }

        /**
         * Méthode permettant à un client de fermer ou d'ouvrir la porte.
         */
        @Override
        public void handlePUT(CoapExchange exchange) {
            byte[] payload = exchange.getRequestPayload();

            try {
                DoorServer.porteOuverte = Boolean.parseBoolean(new String(payload));
                exchange.respond(CHANGED, "{porte ouverte=" + DoorServer.porteOuverte + "}");
            }
            catch(Exception e) {
                e.printStackTrace();
                exchange.respond(BAD_REQUEST, "Invalid String");
            }
        }
    }
}
