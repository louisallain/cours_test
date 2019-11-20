package louis.inf2165.banque;
import java.io.Serializable;

import java.util.Date;

/**
 * Cette classe représente une opération bancaire possible dans l'application.
 * Une opération est représentée par un type (retrait ou dépôt), un montant et une date.
 */
public class Operation implements Serializable {

    /**
     * Le type de l'opération (retrait ou dépôt)
     */
    private TypeOperation type;

    /**
     * Le montant de l'opération.
     */
    private int montant;

    /**
     * La date de l'opération.
     */
    private Date date;
    
    /**
     * Créer un nouvel objet Opérations.
     * La date de l'opération est fixée automatiquement comme étant la date courante à la création de l'opération.
     * @param type le type de l'opération.
     * @param montant le montant de l'opération (ne peut pas être négatif car prend la valeur absolue du montant donnée en paramètre)
     */
    public Operation(TypeOperation type, int montant) {
        
        this.type = type;
        this.montant = Math.abs(montant);
        this.date = new Date();
    }

    /**
     * Donne le type de l'Operation.
     * @return le type de l'Operation.
     */
    public TypeOperation getType() {
        return this.type;
    }

    /**
     * Modifie le type de l'Operation.
     * @param type un nouveau type pour l'opération.
     */
    public void setType(TypeOperation type) {
        this.type = type;
    }

    /**
     * Donne le montant de l'Operation.
     * @return le montant de l'Operation.
     */
    public int getMontant() {
        return this.montant;
    }

    /**
     * Modifie le montant de l'Operation.
     * @param type un nouveau montant pour l'opération.
     */
    public void setMontant(int montant) {
        this.montant = Math.abs(montant);
    }

    /**
     * Donne la date de l'Operation.
     * @return la date de l'Operation.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Modifie la date de l'Operation.
     * @param type une nouvelle date pour l'opération.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Représente un objet Operation sous la forme d'une chaine de caractère.
     * @return une représentation textuelle d'un objet Operation.
     */
    @Override
    public String toString() {
        return "{" +
            " type='" + getType() + "'" +
            ", montant='" + getMontant() + "'" +
            ", date='" + getDate() + "'" +
            "}";
    }

}