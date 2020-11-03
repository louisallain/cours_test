package stock;

import javax.ejb.*;
import javax.interceptor.Interceptors; 
import org.jboss.ejb3.annotation.CacheConfig;

@Stateful
@Remote(Stock.class)
@Interceptors({StockInterceptor.class})
@CacheConfig(maxSize = 1, idleTimeoutSeconds = 1)
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