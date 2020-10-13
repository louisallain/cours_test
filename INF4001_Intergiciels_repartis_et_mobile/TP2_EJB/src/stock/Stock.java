package stock;

public interface Stock {

    /**
     * Ajoute des articles.
     */
    public void ajout(int nb);

    /**
     * Retire des articles.
     */
    public void retrait(int nb);

    /**
     * Retourne le nombre d'articles.
     */
    public int stock();
}