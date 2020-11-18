package tp2.prod.entrepotserviceimpl;

import tp2.services.entrepotservice.*;

public class EntrepotServiceImpl implements EntrepotService {

    private int stock = 0;

    @Override
    public void afficherStock() {
        System.out.println("Stock = " + stock);
    }
    
    @Override
    public void ajouterArticles(int nombre) {
        stock = stock + nombre;
    }
    
    @Override
    public void retirerArticles(int nombre) {
        stock = stock + nombre;
    }
}
