package allain.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class Client {

    public static void main(String args[]) {

        String uri = new String("coap://localhost:5683/maison/cuisineLumiere");
        CoapClient client = new CoapClient(uri);
        CoapResponse response = client.get();
        System.out.println(response.getCode());
        System.out.println(response.getOptions());
        System.out.println(response.getResponseText());
    }
}
