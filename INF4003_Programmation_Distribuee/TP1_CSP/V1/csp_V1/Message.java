package csp_V1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

        ByteArrayInputStream bains = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(bains));
        Message ret = (Message) oin.readObject();
        oin.close();

        return ret;
    }

    public byte[] toBytes() throws IOException {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        oos.flush();
        oos.writeObject(this);
        oos.flush();

        byte[] ret = bos.toByteArray();
        oos.close();

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
