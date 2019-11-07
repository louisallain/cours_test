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

	public static void testReseau() {

		// Test la méthode lireMetro
		try {
			Map<Ligne, List<SimpleEntry<Station, Station>>> donnees = Reseau.lireMetro(cheminFichierMetro);
			donnees.forEach((ligne, listeCouplesStations) -> {
				  
				listeCouplesStations.forEach(coupleStation -> {

					//System.out.println(ligne + " : " + coupleStation.getKey().getNom() + " - " + coupleStation.getValue().getNom());
				});
			});
		}
		catch(IOException ie) {
			System.err.println("Erreur ouverture fichier" + ie);
		}
		
		// test constructeur
		Reseau r1 = new Reseau(cheminFichierMetro);
		//System.out.println(r1.toString());

		// Test getNombreDeStations
		System.out.println(r1.getNombreDeStation());

		// Test ajouterStation
		ArrayList<Station> listeVoisines = new ArrayList<Station>();
		Station s1 = new Station("Gandoulfe");
		r1.ajouterStation(s1);

		// Test ajouterVoisines
		listeVoisines.add(new Station("Porte de Vincennes"));
		listeVoisines.add(new Station("Nation"));
		r1.ajouterVoisines(s1, listeVoisines);
		//System.out.println(r1.toString());

		// Test stationsVoisinesDe
		r1.stationsVoisinesDe("Charles de Gaulle — Étoile").forEach(nomVoisine ->{
			System.out.println(nomVoisine);
		});

		// Test tousLesCheminDeVers
		// List<List<String>> listeChemins = r1.tousLesCheminsDeVers("Château de Vincennes", "Porte Maillot");
		
		// for(List<String> chemins : listeChemins) {

		// 	for(String chemin : chemins) {

		// 		System.out.print(chemin + " -> ");
		// 	}
		// 	System.out.print("\n\n");
		// }

		// Test meilleurCheminDeVers
		// List<String> chemin = r1.meilleurCheminDeVers("Bastille", "Tuileries");
		// System.out.println(chemin);
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

	public static void testLigne() {

		Ligne l1 = new Ligne(1);
		System.out.println(l1.getNumero() == 1);
	}

	public static void main (String[] args){
		testReseau();
	}
}
