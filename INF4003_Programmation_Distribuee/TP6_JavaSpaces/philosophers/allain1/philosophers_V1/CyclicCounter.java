package allain1.philosophers_V1;

public class CyclicCounter extends Counter {
    
    public CyclicCounter(String name, int value) {
        this.name = name;
        this.value = Integer.valueOf(value);
    }

    public CyclicCounter(String name) {
        this.name = name;
    }

    public CyclicCounter() {
        super();
    }

    public Integer resetIf(int nb) {
        if(this.value.intValue() == nb) {
            this.value = Integer.valueOf(this.value.intValue() - nb);
        }
        return this.value;
    }
}
