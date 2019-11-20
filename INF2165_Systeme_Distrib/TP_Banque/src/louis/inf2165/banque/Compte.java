package louis.inf2165.banque;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cette classe représente un compte client de l'application.
 * Chaque compte est identifié par une identité de client et un numéro de compte. 
 * Veillez à fournir à chaque fois un numéro de compte unique parmi tous les numéros de comptes déjà donné.
 * Chaque compte possède un solde, une date d'ouverture (fixée automatiquement) ainsi qu'un historique des opérations réalisées sur ce compte.
 */
public class Compte {

    /**
     * Le numéro du compte.
     */
    private int numCompte;
    /**
     * Le client associé à ce compte.
     */
    private Client idClient;
    /**
     * Le solde du compte.
     */
    private int  solde;
    /**
     * La date d'ouverture du compte.
     */
    private Date date;
    /**
     * L'historique des opérations réalisées sur ce compte.
     */
    private List<Operation> operations;

    /**
     * Créer un nouvel objet Compte.
     * Le solde vaut par défaut 0 à la création et la date est la date de la création.
     * @param numCompte le numéro de compte, doit être unique.
     * @param idClient le client associé à ce compte.
     */
    public Compte(int numCompte, Client idClient) {
        this.numCompte = numCompte;
        this.idClient = idClient;
        this.solde = 0;
        this.date = new Date();
        this.operations = new ArrayList<Operation>();
    }

    /**
     * Donne le numéro de compte du Compte.
     * @return le numéro de compte du Compte.
     */
    public int getNumCompte() {
        return this.numCompte;
    }

    /**
     * Modifie le numéro de compte du Compte.
     * @param numCompte un numéro de compte
     */
    public void setNumCompte(int numCompte) {
        this.numCompte = numCompte;
    }

    /**
     * Donne le client du Compte.
     * @return le client du Compte.
     */
    public Client getIdClient() {
        return this.idClient;
    }

    /**
     * Modifie le client du Compte.
     * @param idClient un idClient pour le Compte
     */
    public void setIdClient(Client idClient) {
        this.idClient = idClient;
    }

    /**
     * Donne le solde du Compte.
     * @return le solde du Compte.
     */
    public int getSolde() {
        return this.solde;
    }

    /**
     * Modifie le solde du Compte.
     * @param idClient un solde pour le Compte
     */
    public void setSolde(int solde) {
        this.solde = solde;
    }

    /**
     * Donne la date du Compte.
     * @return la date du Compte.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Modifie la date de création du Compte.
     * @param idClient une date de création pour le Compte
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Renvoie une copie des opérations bancaires réalisées sur ce compte.
     */
    public List<Operation> getOperations() {
        return new ArrayList<Operation>(operations);
    }

    /**
     * Applique l'opération en paramètre à ce compte.
     * Ajoute également cette opération à la l'historique des opérations de ce compte.
     * @param op l'opération devant être appliquée à ce compte.
     */
    public void applyOperation(Operation op) {

        if(op.getType() == TypeOperation.DEPOT) this.solde = this.solde + op.getMontant();
        else if(op.getType() == TypeOperation.RETRAIT) this.solde = this.solde - op.getMontant();
        
        this.operations.add(op);
    }

    /**
     * Méthode utile permettant de comparer deux objets Compte.
     * La comparaison se fait uniquement sur leurs numéros de comptes.
     * Si ces numéros sont égaux, alors on suppose que les objets sont égaux.
     * @param o l'objet à comparer
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Compte)) {
            return false;
        }
        Compte compte = (Compte) o;
        return this.numCompte == compte.getNumCompte();
    }

    /**
     * Représente un objet Compte sous la forme d'une chaine de caractère.
     * @return une représentation textuelle d'un objet Compte.
     */
    @Override
    public String toString() {
        return "{" +
            " numCompte='" + getNumCompte() + "'" +
            ", idClient='" + getIdClient() + "'" +
            ", solde='" + getSolde() + "'" +
            ", date='" + getDate() + "'" +
            "}";
    }
    
}