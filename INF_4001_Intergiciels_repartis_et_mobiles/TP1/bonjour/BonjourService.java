package bonjour;

public class BonjourService {
	
	private String lang = "fr";
	
	public String disBonjourToutLeMonde() {
		
		String ret = "";
		if(lang.equals("fr")) {
			ret = "Bonjour tout le monde";
		}
		else if(lang.equals("en")) {
			ret = "Hello everyone";
		}
		else if(lang.equals("es")) {
			ret = "Holà todo el mundo";
		}
		return ret;
	}
	
	public String disBonjour(String prenom) {
		
		String ret = "";
		if(lang.equals("fr")) {
			ret = "Bonjour " + prenom;
		}
		else if(lang.equals("en")) {
			ret = "Hello " + prenom;
		}
		else if(lang.equals("es")) {
			ret = "Holà " + prenom;
		}
		
		return ret;
	}
	
	public void setLang(String lang) {
		this.lang = lang;
	}
}
