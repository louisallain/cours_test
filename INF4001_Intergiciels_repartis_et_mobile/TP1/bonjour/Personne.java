package bonjour;

public class Personne {
	
	private String nom;
	private String prenom;
	private int age;

	public Personne() {
		
	}
	
	public String getNom() {
		return this.nom;
	}
	
	public void setNom(String nom) {
		this.nom = nom;
	}
	
		public String getPrenom() {
		return this.prenom;
	}
	
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	
	public int getAge() {
		return this.age;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	public String toString() {
		
		return this.getNom() + " " + this.getPrenom() + " " + this.getAge();
	}
}
