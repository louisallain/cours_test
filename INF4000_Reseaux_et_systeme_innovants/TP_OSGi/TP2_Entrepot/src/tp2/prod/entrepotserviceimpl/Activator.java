package tp2.prod.entrepotserviceimpl;

import tp2.services.entrepotservice.*;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) {

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("Type", "Entrepot");
        context.registerService(EntrepotService.class.getName(), new EntrepotServiceImpl(), props);
    }

    public void stop(BundleContext context) {
        // NOTE: The service is automatically unregistered.
    }
}
