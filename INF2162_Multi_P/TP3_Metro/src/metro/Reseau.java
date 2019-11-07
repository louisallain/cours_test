package metro;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class Reseau {
	
	/**
	 * Le graphe est représenté par une matrice où les indices (abscisses ou ordonnées) sont des noeuds du graphe.
	 * Dans la matrice, les intersections (où il y a des 1) représentent des arcs.
	 */
	private int[][] matriceDuGraphe;

	/**
	 * Permet d'associer un nom à un indice de la matrice.
	 */
	private Map<String, Integer> associationNomIndice;

	/**
	 * Sert à la méthode permettant de trouver le meilleur chemin entre deux stations.
	 */
	private List<String> meilleurChemin;

	/**
	 * Initialise la matrice avec un nombre maximal de stations égal à 500 et grâce à la méthode lireMetro.
	 */
	public Reseau(String nomDeFichier_) {
		
		this.matriceDuGraphe = new int[500][500];
		this.associationNomIndice = new HashMap<String, Integer>();
		this.meilleurChemin = new ArrayList<String>();

		try {
			
			Map<Ligne, List<SimpleEntry<Station, Station>>> donnees = Reseau.lireMetro(nomDeFichier_);
			Set<Station> setDesStations = new HashSet<Station>();

			donnees.forEach((ligne, listeCouplesStations) -> {
				  
				listeCouplesStations.forEach(coupleStation -> {

					Station stationGauche = coupleStation.getKey();
					Station stationDroite = coupleStation.getValue();

					if(this.associationNomIndice.containsKey(stationGauche.getNom()) == false) {
						this.associationNomIndice.put(stationGauche.getNom(), this.associationNomIndice.size());
					}
					if(this.associationNomIndice.containsKey(stationDroite.getNom()) == false) {
						this.associationNomIndice.put(stationDroite.getNom(), this.associationNomIndice.size());
					}

					int x = this.associationNomIndice.get(stationGauche.getNom());
					int y = this.associationNomIndice.get(stationDroite.getNom());
					
					this.matriceDuGraphe[x][y] = 1;
					this.matriceDuGraphe[y][x] = 1;
				});
			});
		}
		catch(IOException ie) {

			System.err.println("Problème d'ouverture du fichier." + ie);
		}
	}

	public void meilleurCheminDeVersUtil(String nomS1_, String nomS2_, List<String> dejaVisite_, List<String> chemin_) {

		List<String> tmpChemin = new ArrayList<String>(chemin_);
		tmpChemin.add(nomS1_);

		List<String> tmpDejaVisite = new ArrayList<String>(dejaVisite_);
		tmpDejaVisite.add(nomS1_);

		if(nomS1_.equals(nomS2_)) {
			this.meilleurChemin.addAll(tmpChemin);
		}

		else {
			
			for(String voisine : this.stationsVoisinesDe(nomS1_)) {

				if(tmpDejaVisite.contains(voisine) == false) {

					boolean continuer = true;
					if(this.meilleurChemin.size() != 0 && this.meilleurChemin.size() <  tmpChemin.size()) {
						continuer = false;
					}
					if(continuer) {
						this.meilleurCheminDeVersUtil(voisine, nomS2_, tmpDejaVisite, tmpChemin);
					}
				}
			}
		}
	}

	public List<String> meilleurCheminDeVers(String nomS1_, String nomS2_) {

		String nomS1 = Reseau.uniformeNom(nomS1_);
		String nomS2 = Reseau.uniformeNom(nomS2_);

		List<String> dejaVisite = new ArrayList<String>();
		List<String> chemin = new ArrayList<String>();

		this.meilleurCheminDeVersUtil(nomS1, nomS2, dejaVisite, chemin);
		return this.meilleurChemin;
	}

	public String toString() {

		String ret = "";
		for(int[] ligne : this.matriceDuGraphe) {

			ret += Arrays.toString(ligne) + "\n";
		}
		return ret;
	}

	public void ajouterStation(Station station_) {

		String nomStation = Reseau.uniformeNom(station_.getNom());
		
		if(this.associationNomIndice.containsKey(nomStation) == false) this.associationNomIndice.put(nomStation, this.associationNomIndice.size());
	}

	public void ajouterVoisines(Station station_, List<Station> stationsVoisines_) {

		String nomStation = Reseau.uniformeNom(station_.getNom());

		if(stationsVoisines_ != null && this.associationNomIndice.containsKey(nomStation) == true) {
			int indiceStation = this.associationNomIndice.get(nomStation);

			stationsVoisines_.forEach(voisine_ -> {

				if(this.associationNomIndice.containsKey(voisine_.getNom())) {

					int indiceStationVoisine = this.associationNomIndice.get(voisine_.getNom());
					this.matriceDuGraphe[indiceStation][indiceStationVoisine] = 1;
					this.matriceDuGraphe[indiceStationVoisine][indiceStation] = 1;
				}
			});
		}
	}

	public ArrayList<String> stationsVoisinesDe(String nomDeStation_) {

		String nomStation = Reseau.uniformeNom(nomDeStation_);		
		ArrayList<String> voisines = new ArrayList<String>();
		if(this.associationNomIndice.containsKey(nomStation) == true) {
			int indiceStation = this.associationNomIndice.get(nomStation);
			
			for(int i = 0; i < this.associationNomIndice.size(); i++) {

				if(this.matriceDuGraphe[indiceStation][i] == 1) {
					
					Integer indiceVoisine = new Integer(i);
					String tmpVoisine = this.associationNomIndice
						.entrySet()
						.stream()
						.filter(entry -> indiceVoisine.equals(entry.getValue()))
						.map(Map.Entry::getKey)
						.findFirst()
						.get();
					voisines.add(tmpVoisine);
				}
			}
		}
		
		return voisines;
	}

	public int getNombreDeStation() {

		return this.associationNomIndice.size();
	}

	/**
	 * Normalise un nom de métro en le mettant en minuscule mais également en retirant les accents, les espaces, les apostrophes et les tirets. 
	 */
	public static String uniformeNom(String nom_) {

		return Normalizer
				.normalize(nom_, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "") // accent
				.replaceAll("\\p{Punct}", "") // poncutation
				.replaceAll("\\p{Space}", "") // espaces
				.toLowerCase();
	}

	 /**
     * Renvoie une lise d'objets correspondant au fichier du métro parisien.
     * @param  nomFichier_ le nom du fichier texte représentant le métro parisien
     * @return             La liste renvoyée est de la forme Liste[ligne(Ligne), station(Station), station(Station)]
     * @throws IOException Erreur renvoyée en cas de mauvaise lecture du fichier
     */
    public static Map<Ligne, List<SimpleEntry<Station, Station>>> lireMetro(String nomFichier_) throws IOException {

    	Map<Ligne, List<SimpleEntry<Station, Station>>> donneesRet;

    	try (BufferedReader br = new BufferedReader(new FileReader(nomFichier_))) {
	        
    		String uneLigne = null;
    		donneesRet = new HashMap<Ligne, List<SimpleEntry<Station, Station>>>();
    		List<SimpleEntry<Station, Station>> stations = new ArrayList<SimpleEntry<Station, Station>>();

	        while((uneLigne = br.readLine()) != null) {
	        	
	        	StringTokenizer st = new StringTokenizer(uneLigne);
	        	Ligne ligne = new Ligne(Integer.parseInt(st.nextToken()));
	        	st.nextToken("\"");
	        	Station s1 = new Station(Reseau.uniformeNom(st.nextToken("\"")));
	        	st.nextToken("\"");
	        	Station s2 = new Station(Reseau.uniformeNom(st.nextToken("\"")));

	        	stations.add(new SimpleEntry<Station, Station>(s1, s2));
	        	donneesRet.put(ligne, stations);
	        }
	    }

	    return donneesRet;
    }
}
