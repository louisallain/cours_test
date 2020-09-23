package csp_V0;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message extends Object implements Serializable {

    public static final int CONTENT_MAX_SIZE = 65536;
    private String content;
    private int destination_id;
    private int source_id;
    private String tag;

    public Message(int dest_id, String tag, String content) {

        this.destination_id = dest_id;
        this.tag = tag;
        if(content.length() <= CONTENT_MAX_SIZE) {
            this.content = content;
        } else {
            System.err.println("[Message : constructor] Content length is too big. Default content is '' .");
            this.content = "";
        }
    }

    void setSourceId(int src_id) {

        this.source_id = src_id;
    }

    public static Message fromBytes(byte[] bytes, int length) throws IOException, ClassNotFoundException, ClassCastException {

        
        Message ret;
        ObjectInput oin = new ObjectInputStream(new ByteArrayInputStream(bytes, 0, length));
        ret = (Message) oin.readObject();

        return ret;
    }

    public byte[] toBytes() throws IOException {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput oout = new ObjectOutputStream(bos);
        oout.writeObject(this);
        byte ret[] = bos.toByteArray();
        oout.close();
        bos.close();

        return ret;
    }

    public int getSourceId() {
        return this.source_id;
    } 

    public int getDestinationId() {
        return this.destination_id;
    }

    public String getTag() {
        return this.tag;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return "Destination = " + this.destination_id + "\n" + "Source = " + this.source_id + "\n" + "Tag = " + this.tag + "\n" + "Content = " + this.content + "\n";
    }
}
