package louis.client.bonjourserviceclient;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

import louis.services.bonjourservice.BonjourService;

public class Activator implements BundleActivator {
    
    public void start(BundleContext context) throws BundleException {

        try {
            ServiceReference[] refs = context.getServiceReferences(BonjourService.class.getName(), "(Language=*)");
            if(refs != null) {
                BonjourService bs = (BonjourService) context.getService(refs[0]);
                bs.bonjour();
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
