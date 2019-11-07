package metro;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

public class Reseau {
	
	/**
	 * Objet de la librairie graphstream
	 */
	private Graph grapheDuReseau;

	public Reseau(String nomDeFichier_) {

		try {
			this.grapheDuReseau = CreeReseauAPartirDuFichier(nomDeFichier_);
		}
		catch(IOException e) {
			System.err.println(e);
		}
	}
	
	public Graph getGraphe() {
		
		return this.grapheDuReseau;
	}

	public ArrayList<String> stationsVoisinesDe(String nomDeStation_) {
		
		Iterator<Node> voisinsIt = this.grapheDuReseau.getNode(nomDeStation_).getNeighborNodeIterator();
		ArrayList<String> voisins = new ArrayList<String>();
		Node tmpNode;
		if(voisinsIt != null) {
			while(voisinsIt.hasNext()) {
				
				tmpNode = voisinsIt.next();

				voisins.add((String)tmpNode.getId());
			}
		}
		return voisins;
	}

	public int getNombreDeStation() {

		int nbS = 0;

		for(Node node : this.grapheDuReseau) {
			nbS = nbS + 1;
		}
		return nbS;
	}
	
	/**
	 * Créer un graphe à partir de la bibliothèque graphstream et de la méthode Utils.lireMetro.
	 */
	private static Graph CreeReseauAPartirDuFichier(String nomDeFichier_) throws IOException {

		Graph graph = new MultiGraph("Métro Paris");
		try {
			
			Map<Ligne, List<SimpleEntry<Station, Station>>> donnees = Utils.lireMetro(nomDeFichier_);
			donnees.forEach((ligne, listeCouplesStations) -> {
				  
				listeCouplesStations.forEach(coupleStation -> {

						String nomS1 = coupleStation.getKey().getNom();
						String nomS2 = coupleStation.getValue().getNom();

						try {
							graph.addNode(nomS1);
						}
						catch(IdAlreadyInUseException e) {}
						
						try{
							graph.addNode(nomS2);
						}
						catch(IdAlreadyInUseException e) {}
						finally {
							try {
								graph.addEdge(nomS1+nomS2, nomS1, nomS2);
							}
							catch(IdAlreadyInUseException e) {}
						}
				});
			});
			for (Node node : graph) {
		        node.addAttribute("ui.label", node.getId());
		    }
		}
		catch(IOException ie) {

			System.err.println("Problème d'ouverture du fichier.");
			System.err.println(ie);
		}
		
		return graph;
	}
}
