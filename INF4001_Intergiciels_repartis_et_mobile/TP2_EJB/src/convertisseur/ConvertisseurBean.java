package convertisseur;

import javax.ejb.*;

@Stateless
@Remote(Convertisseur.class)
public class ConvertisseurBean implements Convertisseur {

    public double convertis(double euros) {
        return (Double)(euros * 6.55957);
    }
}