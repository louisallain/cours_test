package allain.coap;

import java.io.InputStreamReader;
import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class Client {

    /**
     * Commande permettant de questionner le serveur sur l'état des capteurs.
     * @param args
     */
    public static void main(String args[]) {

        /*
        // Requête générale
        CoapClient client = new CoapClient("coap://localhost:5683/.well-known/core");
        CoapResponse response1 = client.get();
        System.out.println(response1.getCode());
        System.out.println(response1.getOptions());
        System.out.println(response1.getResponseText());
        client.shutdown();
        */

        CoapClient client;
        while(true) {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));

            System.out.println("Entrer 'q' pour terminer le programme.");
            System.out.println("Piece voulue : (cuisine, sejour ou salon) ou controle de la porte (porte) : ");
            String piece = scanner.nextLine();


            if(piece.equals("q")) System.exit(0); // vérifie seulement au début de la séquence si on souhaite arrêter le programme
            if(!piece.equals(Server.CUISINE) && !piece.equals(Server.SEJOUR) && !piece.equals(Server.SALON) && !piece.equals("porte")) {
                System.out.println("Commande inconnue"); continue;
            }
            System.out.println("Commande selectionnee : " + piece);

            if(piece.equals("porte")) {
                System.out.println("Voir l'etat de la porte (etat) ou la controler (ctrl) :");
                String porteType = scanner.nextLine();
                if(!porteType.equals("etat") && !porteType.equals("ctrl")) {
                    System.out.println("Commande inconnue"); continue;
                }
                if(porteType.equals("etat")) {
                    String uri = new String("coap://localhost:" + DoorServer.port + "/porte");
                    client = new CoapClient(uri);
                    CoapResponse response = client.get();
                    System.out.println(response.getCode());
                    System.out.println(response.getOptions());
                    System.out.println(response.getResponseText());
                    client.shutdown();
                    continue;
                }
                if(porteType.equals("ctrl")) {
                    System.out.println("Ouvrir (true) ou fermer (false) :");
                    String valuePorte = scanner.nextLine();
                    if(!valuePorte.equals("true") && !valuePorte.equals("false")) {
                        System.out.println("Valeur incorrecte !"); 
                        continue;
                    }
                    String uri = new String("coap://localhost:" + DoorServer.port + "/porte");
                    client = new CoapClient(uri);
                    CoapResponse response = client.put(valuePorte, 0);
                    System.out.println(response.getCode());
                    System.out.println(response.getOptions());
                    System.out.println(response.getResponseText());
                    client.shutdown();
                    continue;
                }
            }

            System.out.println("Actionneur : (lumiere ou presence) :");
            String actionneur = scanner.nextLine();
            if(!actionneur.equals("lumiere") && !actionneur.equals("presence")) {
                System.out.println("Actionneur inconnu"); 
                continue;
            }
            System.out.println("Actionneur selectionne : " + actionneur);

            if(actionneur.equals("lumiere")) {
                System.out.println("Obtenir les infos du capteur (infos) ou actionner la lumiere (action) :");
                String type = scanner.nextLine();
                if(!type.equals("infos") && !type.equals("action")) {
                    System.out.println("Type inconnu"); 
                    continue;
                }
                System.out.println("Type selectionne : " + type);
                if(type.equals("infos")) {
                    String uri = new String("coap://localhost:5683/maison/lumiere:"+piece);
                    client = new CoapClient(uri);
                    CoapResponse response = client.get();
                    System.out.println(response.getCode());
                    System.out.println(response.getOptions());
                    System.out.println(response.getResponseText());
                    client.shutdown();
                }
                else {
                    System.out.println("Allumer (true) ou éteindre (false) :");
                    String value = scanner.nextLine();
                    if(!value.equals("true") && !value.equals("false")) {
                        System.out.println("Valeur incorrecte !"); 
                        continue;
                    }
                    String uri = new String("coap://localhost:5683/maison/lumiere:"+piece);
                    client = new CoapClient(uri);
                    CoapResponse response = client.put(value, 0);
                    System.out.println(response.getCode());
                    System.out.println(response.getOptions());
                    System.out.println(response.getResponseText());
                    client.shutdown();
                }
            }
            else {
                System.out.println("Obtention des informations du capteur de presence de la piece " + piece + " en cours ...");
                String uri = new String("coap://localhost:5683/maison/presence:"+piece);
                client = new CoapClient(uri);
                CoapResponse response = client.get();
                System.out.println(response.getCode());
                System.out.println(response.getOptions());
                System.out.println(response.getResponseText());
                client.shutdown();
            }
        }
    }
}
