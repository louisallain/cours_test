package client;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import bonjour.Personne;

public class BonjourClient {
    public static void main(String[] args1) throws AxisFault {
        
        RPCServiceClient serviceClient = new RPCServiceClient();
    
        Options options = serviceClient.getOptions();

        EndpointReference targetEPR = new EndpointReference("http://localhost:8080/axis2/services/BonjourService/");
        options.setTo(targetEPR);

        // Operation disBonjour(Personne)
        QName opDisBonjour = new QName("http://bonjour", "disBonjour");

        Personne p = new Personne();
        p.setNom("Allain");
        p.setPrenom("Louis");
        p.setAge(22);

        Object[] opDisBonjourArgs = new Object[] { p };
        Class[] returnTypes = new Class[] { bonjour.Personne.class };
        
        Object[] response = serviceClient.invokeBlocking(opDisBonjour, opDisBonjourArgs, returnTypes);

        Personne result = (Personne) response[0];
        
        if (result == null) {
            System.out.println("Saying hello to null.");
            return;
        }

        // Displaying the result
        System.out.println(result.toString());
    }
}
