package client;

import convertisseur.Convertisseur;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class ConvertisseurClient {

    public static void main(String[] args) throws Exception {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "localhost");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        Context ctx = new InitialContext(env);
        Convertisseur convertisseur = (Convertisseur) ctx.lookup("ConvertisseurBean/remote");

        System.out.println("10 euros = " + convertisseur.convertis(10) + " FF");
        System.out.println("1521,25 euros = " + convertisseur.convertis(1521.25) + " FF");
        System.out.println("-14 euros = " + convertisseur.convertis(-14) + " FF");
    }
}