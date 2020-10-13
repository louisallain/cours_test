package client;

import stock.Stock;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

public class StockClient {

    public static void main(String[] args) throws Exception {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.NamingContextFactory");
        env.put(Context.PROVIDER_URL, "localhost");
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        Context ctx = new InitialContext(env);

        List<Stock> listOfStock = new ArrayList<>();
        listOfStock.add((Stock) ctx.lookup("StockBean/remote"));
        listOfStock.add((Stock) ctx.lookup("StockBean/remote"));

        // Stock num 1
        listOfStock.get(0).ajout(10);
        listOfStock.get(0).ajout(-10);
        System.out.println("Stock num 1 = " + listOfStock.get(0).stock());
        listOfStock.get(0).retrait(5);
        listOfStock.get(0).retrait(-1);
        System.out.println("Stock num 1 = " + listOfStock.get(0).stock());

        // Stock num 2
        System.out.println("Stock num 2 = " + listOfStock.get(1).stock());
        listOfStock.get(1).retrait(10);
        System.out.println("Stock num 2 = " + listOfStock.get(1).stock());
        listOfStock.get(1).ajout(4000000);
        System.out.println("Stock num 2 = " + listOfStock.get(1).stock());
    }
}