package metro;

import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

/**
 * Test les différentes classes du TP Métro.
 * Tous les affichages doivent être "true" sinon il y 
 * a une erreur. Ou alors si ce n'est pas un booléen qui est affiché, il 
 * s'agit d'un test visuel.
 */
public final class Test {

	private static String cheminFichierMetro = "./res/Metro.txt";

	public static void testLigne() {

		Ligne l1 = new Ligne(1);
		System.out.println(l1.getNumero() == 1);
	}

	public static void testReseau() {
			
		Reseau r1 = new Reseau(cheminFichierMetro);
		r1.getGraphe().display();
		System.out.println("Nombre de stations : " + r1.getNombreDeStation());

		ArrayList<String> voisinsDeCharles = r1.stationsVoisinesDe("Concorde");
		for(String v : voisinsDeCharles) {
			System.out.println(v);
		}
	}

	public static void testUtils() {
		
		try {
			Map<Ligne, List<SimpleEntry<Station, Station>>> donnees = Utils.lireMetro(cheminFichierMetro);
			donnees.forEach((ligne, listeCouplesStations) -> {
				  
				listeCouplesStations.forEach(coupleStation -> {

					System.out.println(ligne + " : " + coupleStation.getKey().getNom() + " - " + coupleStation.getValue().getNom());
				});
			});
		}
		catch(IOException ie) {
			System.err.println("Erreur ouverture fichier" + ie);
		}
	}

	public static void testStation() {

		Station s = new Station("Berry-U");
		Ligne l1 = new Ligne(1);
		Ligne l2 = new Ligne(2);

		s.ajouteLigne(l1);
		s.ajouteLigne(l2);

		s.forEach(l -> {

			System.out.println(l);
		});

		System.out.println(s.getNom().compareTo("Berry-U") == 0);
	}

	public static void main (String[] args){
		
		testReseau();
	}
}
