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

        // Requête générale
        CoapClient client = new CoapClient("coap://localhost:5683/.well-known/core");
        CoapResponse response1 = client.get();
        System.out.println(response1.getCode());
        System.out.println(response1.getOptions());
        System.out.println(response1.getResponseText());
        client.shutdown();

        while(true) {
            Scanner scanner = new Scanner(new InputStreamReader(System.in));

            System.out.println("Piece voule : (cuisine, sejour ou salon) :");
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

                // test observer
                CoapObserveRelation relation = client.observe(new CoapHandler(){
                    @Override public void onLoad(CoapResponse response) {
						String content = response.getResponseText();
						System.out.println("NOTIFICATION: " + content);
					}
					
					@Override public void onError() {
						System.err.println("OBSERVING FAILED");
					}
                });
                client.shutdown();
                relation.proactiveCancel(); // désactive la relation d'observer
            }
        }
    }
}
