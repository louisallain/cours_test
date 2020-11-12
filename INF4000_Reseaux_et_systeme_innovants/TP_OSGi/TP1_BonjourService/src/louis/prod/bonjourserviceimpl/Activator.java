package louis.prod.bonjourserviceimpl;

import louis.services.bonjourservice.BonjourService;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) {

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("Language", "French");
        context.registerService(BonjourService.class.getName(), new BonjourServiceImpl(), props);
    }

    public void stop(BundleContext context) {
        // NOTE: The service is automatically unregistered.
    }
}
