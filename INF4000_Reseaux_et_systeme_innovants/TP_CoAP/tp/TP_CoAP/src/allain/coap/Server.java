package allain.coap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;

import java.io.InputStreamReader;
import java.util.Scanner;

public class Server {

    /* Données des capteurs de la cuisine */
    static boolean lumiereCuisine = false;
    static int luxCuisine = 0;
    static int nbPersonnesCuisine = 0;

    /* Données des capteurs du salon */
    static boolean lumiereSalon = false;
    static int luxSalon = 0;
    static int nbPersonnesSalon = 0;

    /* Données des capteurs du séjour */
    static boolean lumiereSejour = false;
    static int luxSejour = 0;
    static int nbPersonnesSejour = 0;

    static String CUISINE = "cuisine";
    static String SEJOUR = "sejour";
    static String SALON = "salon";

    /**
     * Demarre le serveur CoAP.
     * Démarre une commande permettant de simuler des valeurs pour les capteurs.
     */
    public static void main(String[] args) {

        // binds on UDP port 5683
        CoapServer server = new CoapServer();

        CoapResource path = new CoapResource("maison");

        path.add(new LumiereResource(Server.CUISINE));
        path.add(new LumiereResource(Server.SALON));
        path.add(new LumiereResource(Server.SEJOUR));

        PresenceResource cuisinePresenceResource = new PresenceResource(Server.CUISINE);
        PresenceResource salonPresenceResource = new PresenceResource(Server.SALON);
        PresenceResource sejourPresenceResource = new PresenceResource(Server.SEJOUR);
        
        path.add(cuisinePresenceResource);
        path.add(salonPresenceResource);
        path.add(sejourPresenceResource);
        server.add(path);

        server.start();
        System.out.println("Server started");

        while(true) {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));

            System.out.println("Piece a actionner : (cuisine, sejour ou salon) :");
            String piece = scanner.nextLine();
            if(!piece.equals(Server.CUISINE) && !piece.equals(Server.SEJOUR) && !piece.equals(Server.SALON)) {
                System.out.println("Piece inconnue"); continue;
            }
            System.out.println("Pieces selectionnee : " + piece);

            System.out.println("Actionneur : (lumiere ou presence) :");
            String actionneur = scanner.nextLine();
            if(!actionneur.equals("lumiere") && !actionneur.equals("presence")) {
                System.out.println("Actionneur inconnu"); 
                continue;
            }
            System.out.println("Actionneur selectionne : " + actionneur);

            if(actionneur.equals("lumiere")) {
                System.out.println("Nouvelle valeur de lumiere : ([0; 500]) :");
                int lux = scanner.nextInt();
                if(lux < 0 || lux > 500) {
                    System.out.println("Valeur incorrecte"); 
                    continue;
                }

                if(piece.equals(Server.CUISINE)) {
                    luxCuisine = lux;
                    if(luxCuisine < 300 && nbPersonnesCuisine > 0) lumiereCuisine = true;
                    else if(luxCuisine >= 300) lumiereCuisine = false;
                }
                else if(piece.equals(Server.SALON)) {
                    luxSalon = lux;
                    if(luxSalon < 300 && nbPersonnesSalon > 0) lumiereSalon = true;
                    else if(luxSalon >= 300) lumiereSalon = false;
                }
                else if(piece.equals(Server.SEJOUR)) {
                    luxSejour = lux;
                    if(luxSejour < 300 && nbPersonnesSejour > 0) lumiereSejour = true;
                    else if(luxSejour >= 300) lumiereSejour = false;
                }
            }
            else {
                System.out.println("Nouveau nombre de personne : (>= 0) :");
                int nbPersonnes = scanner.nextInt();
                if(nbPersonnes < 0) {
                    System.out.println("Valeur incorrecte"); 
                    continue;
                }

                if(piece.equals(Server.CUISINE)) {
                    nbPersonnesCuisine = nbPersonnes;
                    cuisinePresenceResource.changed(); // indique que la ressource à changée
                    if(luxCuisine < 300 && nbPersonnesCuisine > 0) lumiereCuisine = true;
                    else if(nbPersonnesCuisine == 0) lumiereCuisine = false;
                }
                else if(piece.equals(Server.SALON)) {
                    nbPersonnesSalon = nbPersonnes;
                    salonPresenceResource.changed(); // indique que la ressource à changée
                    if(luxSalon < 300 && nbPersonnesSalon > 0) lumiereSalon = true;
                    else if(nbPersonnesSalon == 0) lumiereSalon = false;
                }
                else if(piece.equals(Server.SEJOUR)) {
                    nbPersonnesSejour = nbPersonnes;
                    sejourPresenceResource.changed(); // indique que la ressource à changée
                    if(luxSejour < 300 && nbPersonnesSejour > 0) lumiereSejour = true;
                    else if(nbPersonnesSejour == 0) lumiereSejour = false;
                }
            }
        }
    }

    public static class LumiereResource extends CoapResource {

        String nomPiece;

        /**
         * Créer une nouvelle ressource d'un capteur de lumière pour une pièce.
         * @param nomPiece le nom de la pièce.
         */
        public LumiereResource(String nomPiece) {
            super("lumiere:"+nomPiece);
            this.nomPiece = nomPiece;
            getAttributes().setTitle("Resource capteur de lumiere:"+nomPiece);
        }

        /**
         * Méthode permettant à un client d'obtenir les informations relatives au capteur de lumière d'un pièce.
         */
        @Override
        public void handleGET(CoapExchange exchange) {
            if(this.nomPiece.equals(Server.CUISINE)) {
                exchange.respond("{lux = " + luxCuisine + ", allumee = " + lumiereCuisine + "}");
            }
            else if(this.nomPiece.equals(Server.SALON)) {
                exchange.respond("{lux = " + luxSalon + ", allumee = " + lumiereSalon + "}");
            }
            else if(this.nomPiece.equals(Server.SEJOUR)) {
                exchange.respond("{lux = " + luxSejour + ", allumee = " + lumiereSejour + "}");
            }
            else {
                exchange.respond(BAD_REQUEST, "Piece inconnue !");
            }
        }

        /**
         * Méthode permettant à un client d'allumer ou d'éteindre la lumière d'une pièce.
         */
        @Override
        public void handlePUT(CoapExchange exchange) {
            byte[] payload = exchange.getRequestPayload();

            if(this.nomPiece.equals(Server.CUISINE)) {
                try {
                    lumiereCuisine = Boolean.parseBoolean(new String(payload));
                    exchange.respond(CHANGED, "{lux = " + luxCuisine + ", allumee = " + lumiereCuisine + "}");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    exchange.respond(BAD_REQUEST, "Invalid String");
                }
            }
            else if(this.nomPiece.equals(Server.SALON)) {
                try {
                    lumiereSalon = Boolean.parseBoolean(new String(payload));
                    exchange.respond(CHANGED, "{lux = " + luxSalon+ ", allumee = " + lumiereSalon + "}");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    exchange.respond(BAD_REQUEST, "Invalid String");
                }
            }
            else if(this.nomPiece.equals(Server.SEJOUR)) {
                try {
                    lumiereSejour = Boolean.parseBoolean(new String(payload));
                    exchange.respond(CHANGED, "{lux = " + luxSejour + ", allumee = " + lumiereSejour + "}");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    exchange.respond(BAD_REQUEST, "Invalid String");
                }
            }
            else {
                exchange.respond(BAD_REQUEST, "Piece inconnue !");
            }
        }
    }

    public static class PresenceResource extends CoapResource {

        /**
         * Créer une nouvelle ressource d'un capteur de présence pour une pièce.
         * @param nomPiece le nom de la pièce.
         */
        String nomPiece;

        public PresenceResource(String nomPiece) {
            super("presence:"+nomPiece);
            this.nomPiece = nomPiece;

            this.setObservable(true); // indique que cette ressource peut être observée (nécessaire pour la porte automatique dans ce cas)
            this.setObserveType(Type.CON); // type de message confirmable
            this.getAttributes().setObservable(); // mark observable in the Link-Format

            this.getAttributes().setTitle("Resource capteur de presence:"+nomPiece);
        }

        /**
         * Méthode permettant à un client d'obtenir les informations relatives au capteur de présence d'un pièce.
         */
        @Override
        public void handleGET(CoapExchange exchange) {
            if(this.nomPiece.equals(Server.CUISINE)) {
                exchange.respond("{nombre de personne=" + nbPersonnesCuisine + "}");
            }
            else if(this.nomPiece.equals(Server.SALON)) {
                exchange.respond("{nombre de personne=" + nbPersonnesSalon + "}");
            }
            else if(this.nomPiece.equals(Server.SEJOUR)) {
                exchange.respond("{nombre de personne=" + nbPersonnesSejour + "}");
            }
            else {
                exchange.respond(BAD_REQUEST, "Piece inconnue !");
            }
        }
    }
}
