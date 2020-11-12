package epicerie;

import java.io.Serializable;
import javax.persistence.*;

@Entity
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    private int code; // l'id
    private double prix;
    private String nom;

    public Article(int code, double prix, String nom) {
        this.code = code;
        this.prix = prix;
        this.nom = nom;
    }

    public int getCode() { return this.code; }
    public double getPrix() { return this.prix; }
    public String getNom() { return this.nom; }

    public void setCode(int code) { this.code = code; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setNom(String nom) {this.nom = nom; }

    @Override
    public String toString() { return "{Article : { code : " + this.getCode() + " , nom : " + this.getNom() + " , prix : " + this.getPrix() + "} }"; }
}