package pop;

import java.io.File;

public class Message {

    private File file;
    private boolean marked;

    public Message(File file, boolean marked) {
        this.file = file;
        this.marked = marked;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isMarked() {
        return this.marked;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setMarked(boolean m) {
        this.marked = m;
    }
}