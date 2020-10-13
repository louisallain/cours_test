package stock;

import javax.ejb.*;

@Stateful
@Remote(Stock.class)
public class StockBean implements Stock {

    private int stock;

    /**
     * Ajoute des articles.
     */
    public void ajout(int nb) {
        this.stock += Math.abs(nb);
    }

    /**
     * Retire des articles.
     */
    public void retrait(int nb) {
        this.stock -= Math.abs(nb);
    }

    /**
     * Retourne le nombre d'articles.
     */
    public int stock() {
        return this.stock;
    }   
}