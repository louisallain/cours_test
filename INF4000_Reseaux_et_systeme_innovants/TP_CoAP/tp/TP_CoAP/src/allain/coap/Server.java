package allain.coap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;

import java.io.InputStreamReader;
import java.util.Scanner;

public class Server {

    static boolean lumiereCuisine = false;
    static int luxCuisine = 0;
    static int nbPersonnesCuisine = 0;

    /**
     * Demarre le serveur.
     */
    public static void main(String[] args) {

        // binds on UDP port 5683
        CoapServer server = new CoapServer();

        CoapResource path = new CoapResource("maison");
        path.add(new CuisineLumiereResource());
        path.add(new CuisinePresenceResource());
        server.add(path);

        server.start();
        System.out.println("Server started");

        while(true) {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));

            System.out.println("Piece a actionner : (cuisine, sejour ou salon) :");
            String piece = scanner.nextLine();
            if(!piece.equals("cuisine") && !piece.equals("sejour") && !piece.equals("salon")) {
                System.out.println("Piece inconnue"); continue;
            }
            System.out.println("Pieces selectionnee : " + piece);

            System.out.println("Actionneur : (lumiere ou presence) :");
            String actionneur = scanner.nextLine();
            if(!actionneur.equals("lumiere") && !actionneur.equals("presence")) {
                System.out.println("Actionneur inconnu"); continue;
            }
            System.out.println("Actionneur selectionne : " + actionneur);

            if(actionneur.equals("lumiere")) {
                System.out.println("Nouvelle valeur de lumiere : ([0; 500]) :");
                int lux = scanner.nextInt();
                if(lux < 0 || lux > 500) {
                    System.out.println("Valeur incorrecte"); continue;
                }

                if(piece.equals("cuisine")) {
                    luxCuisine = lux;
                    if(luxCuisine < 300 && nbPersonnesCuisine > 0) lumiereCuisine = true;
                    else if(luxCuisine >= 300) lumiereCuisine = false;
                }
            }
            else {
                System.out.println("Nouveau nombre de personne : (>= 0) :");
                int nbPersonnes = scanner.nextInt();
                if(nbPersonnes < 0) {
                    System.out.println("Valeur incorrecte"); continue;
                }

                if(piece.equals("cuisine")) {
                    nbPersonnesCuisine = nbPersonnes;
                    if(luxCuisine >= 300 && nbPersonnesCuisine > 0) lumiereCuisine = true;
                    else if(nbPersonnesCuisine == 0) lumiereCuisine = false;
                }
            }
        }
    }

    public static class CuisineLumiereResource extends CoapResource {

        public CuisineLumiereResource() {
            super("cuisineLumiere");
            getAttributes().setTitle("Cuisine Lumiere Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond("Allumee = " + lumiereCuisine);
        }
    }

    public static class CuisinePresenceResource extends CoapResource {

        public CuisinePresenceResource() {
            super("cuisinePresence");
            getAttributes().setTitle("Cuisine Presence Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond("Nombre de personnes dans la cuisine = " + nbPersonnesCuisine);
        }
    }
}
