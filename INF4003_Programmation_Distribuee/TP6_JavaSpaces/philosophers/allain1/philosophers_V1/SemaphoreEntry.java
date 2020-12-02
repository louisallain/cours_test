package allain1.philosophers_V1;

public class SemaphoreEntry extends MyEntry {
    
    public String resource;

    public SemaphoreEntry(String resource) {
        this.resource = resource;
    }

    public SemaphoreEntry() {
        this("SemaphoreDefault");
    }
}
