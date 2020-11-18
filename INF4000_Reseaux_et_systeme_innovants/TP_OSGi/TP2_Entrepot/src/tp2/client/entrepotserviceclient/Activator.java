package tp2.client.entrepotserviceclient;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

import tp2.services.entrepotservice.*;

public class Activator implements BundleActivator {
    
    public void start(BundleContext context) throws BundleException {

        try {
            ServiceReference[] refs = context.getServiceReferences(EntrepotService.class.getName(), "(Type=*)");
            if(refs != null) {
                EntrepotService es = (EntrepotService) context.getService(refs[0]);
                es.afficherStock();
                es.ajouterArticles(2);
                es.afficherStock();
                es.retirerArticles(1);
                es.afficherStock();
                context.ungetService(refs[0]);
            }
        }
        catch(InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    public void stop(BundleContext context) {
        // NOTE: The service is automatically released.
    }
}
