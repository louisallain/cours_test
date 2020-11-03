package stock;

import javax.ejb.*;
import javax.interceptor.AroundInvoke;  
import javax.interceptor.InvocationContext;
import javax.interceptor.Interceptors;
import javax.annotation.*;

public class StockInterceptor {

    @AroundInvoke  
    public Object log(InvocationContext ctx) throws Exception {  
        
        System.out.println("*** Interception par TraceInterceptor");  
        long start = System.currentTimeMillis();  
        try {  
            return ctx.proceed(); // exécute la méthode métier interceptée initialement  
        }  
        catch(Exception e) {  
            throw e;  
        }  
        finally {  
            long time = System.currentTimeMillis() - start;  
            String method = ctx.getClass().getName() + "." + ctx.getMethod().getName() + "()";  
            System.out.println("*** TracingInterceptor : l'invocation de " + method + " a pris " + time + "ms");  
        }  
    }
    
    
    @PostConstruct
    public void notifyPostConstruct(InvocationContext ctx) {
        System.out.println("Nouveau StockBean");
    }  


    @PrePassivate
    public void notifyPrePassivate(InvocationContext ctx) {
        System.out.println("Passivation StockBean");
    }  

    @PostActivate
    public void notifyPostActivate(InvocationContext ctx) {
        System.out.println("Activation StockBean");
    }  
}