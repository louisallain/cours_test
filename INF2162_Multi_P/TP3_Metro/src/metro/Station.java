package metro;

import java.util.Set; 
import java.util.HashSet;
import java.util.Iterator;

public class Station implements Iterable<Ligne> {

	private String nom;
	private Set<Ligne> listeLignes;

	public Station(String nom_) {

		if(nom_ != null) 
			this.nom = nom_;
		else 
			this.nom = "Défaut";

		this.listeLignes = new HashSet<Ligne>();
	}

	public Station(String nom_, Set<Ligne> listeLignes_) {

		this(nom_);
		if(listeLignes_ != null) 
			this.listeLignes = listeLignes_;
	}

	public void ajouteLigne(Ligne ligne_) {
		
		this.listeLignes.add(ligne_);
	} 

	public String getNom() {

		return this.nom;
	}

	public Set<Ligne> getLignes() {

		return this.listeLignes;
	}

	@Override
    public Iterator<Ligne> iterator() {
        return this.listeLignes.iterator();
    }
}