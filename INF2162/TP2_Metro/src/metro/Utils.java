package metro;

import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap.SimpleEntry;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException();
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
	        	Station s1 = new Station(st.nextToken("\""));
	        	st.nextToken("\"");
	        	Station s2 = new Station(st.nextToken("\""));

	        	stations.add(new SimpleEntry<Station, Station>(s1, s2));
	        	donneesRet.put(ligne, stations);
	        }
	    }

	    return donneesRet;
    }
}
