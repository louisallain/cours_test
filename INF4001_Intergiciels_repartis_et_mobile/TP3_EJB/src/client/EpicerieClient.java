package client;

import epicerie.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class EpicerieClient {

    public static void main(String[] args) throws Exception {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "localhost");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        Context ctx = new InitialContext(env);
        Epicerie epicerie = (Epicerie) ctx.lookup("EpicerieBean/remote");

        System.out.println("Avant creation :");
        for(Article a : epicerie.listeArticles()) {
            System.out.println(a);
        }

        /*
        System.out.println("Apres creation :");
        for(Article a : epicerie.listeArticles()) {
            System.out.println(a);
        }
        */
    }
}