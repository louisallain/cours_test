package allain1.philosophers_V1;

import net.jini.core.entry.Entry;

public class Counter extends MyEntry {
    
    /**
     * Nom du compteur.
     */
    String name; 

    /**
     * Valeur courante du compteur.
     */
    Integer value;

    public Counter(String name, int value) {
        this.name = name;
        this.value = Integer.valueOf(value);
    }

    public Counter(String name) {
        this(name, Integer.valueOf(0));
    }

    public Counter() {
        this("CounterDefault", Integer.valueOf(0));
    }

    public Integer decrement() {
        this.value = Integer.valueOf(this.value.intValue() - 1);
        return this.value;
    }

    public Integer increment() {
        this.value = Integer.valueOf(this.value.intValue() + 1);
        return this.value;
    }
}
