package allain1.philosophers_V1;

import net.jini.space.JavaSpace;
import net.jini.core.lease.Lease;

public class CyclicCounterAccessor {
    
    private JavaSpace space;
    private String resource;
    private Integer numberOfPhilosophers;
    private Counter turn;

    public CyclicCounterAccessor(JavaSpace space, String resource, int numberOfPhilosophers) {
        this.space = space;
        this.resource = resource;
        this.numberOfPhilosophers = Integer.valueOf(numberOfPhilosophers);
        this.turn = new CyclicCounter(this.resource, 0);
    }

    public void create() {
        
        try {
            this.space.write(this.turn, null, Lease.FOREVER);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public CyclicCounter waitForTurn(int num) {
        try {
            CyclicCounter tmpl = new CyclicCounter(this.resource, num);
            CyclicCounter curr = (CyclicCounter) this.space.take(tmpl, null, Long.MAX_VALUE);
            return curr;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void passeToNextTurn(CyclicCounter curr) {
        curr.increment();
        curr.resetIf(this.numberOfPhilosophers);
        try {
            this.space.write(curr, null, Lease.FOREVER);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
