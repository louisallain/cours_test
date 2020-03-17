package casa.util.dodwanclient;

import casa.util.Processor;
import casa.util.pdu.BinaryData;
import casa.util.pdu.Pdu;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* Service used to publish messages and subscribe in order to receive published
* messages, using DoDWAN through a DoDWAN NAPI server.
*
* Relies on a {@link Session} instance. The service is active only if the session
* is started, that is, the publishing and reception of messages are inefficient
* when the session is stopped, and sent/received messages are lost (no buffering)
*/
public class PubSubService implements Processor<Pdu>
{
    /**
    * Counter to generate unique tokens for requests
    */
    private static int TKN_CPT = 0;

    /**
    * Singleton instance of this class
    */
    private static PubSubService instance;

    /**
    * Timeout for acknoledgements
    */
    private final int ackTimeout;

    /**
    * true if the client and server share the same file system
    */
    private final boolean sharedFileSystem;

    /**
    * Channel used to send/receive PDUs
    */
    private final PduChannel channel;

    /**
    * Processors of incoming descriptors associated to subscription keys
    */
    private final Map<String, Processor<Descriptor>> processors;

    /**
    * Give a singleton instance of this class
    *
    * @return singleton {@link PubSubService}
    */
    public static PubSubService getInstance()
    {
        if (instance == null) {
            Configuration conf = Configuration.getInstance();
            instance = new PubSubService(conf.sharedFileSystem(), conf.getReplyTimeout());
        }
        return instance;
    }

    /**
    * Private constructor (singleton pattern). Register this instance to receive
    * recv_desc notifications from the server
    *
    * @param ackTimeout the timeout for acknoledgements
    * @param sharedFileSystem true if the client and server share the same file system
    */
    private PubSubService(boolean sharedFileSystem, int ackTimeout) {
        this.channel = Session.getInstance().getChannel();
        this.sharedFileSystem = sharedFileSystem;
        this.ackTimeout = ackTimeout;
        this.processors = new ConcurrentHashMap<>();

        this.channel.putProcessor("recv_desc", this);
    }


    /**
    * Process the recv_desc server notifications
    *
    * @param pdu the PDU received from the server
    */
    @Override
    public void process(Pdu pdu) throws Exception
    {
        String skey = pdu.getString("key");
        if (skey != null) {
            Processor<Descriptor> processor = this.processors.get(skey);
            if (processor != null) {
                Descriptor descriptor = new Descriptor(pdu.getAttributes("desc"));
                descriptor.setMid(pdu.getString("mid"));
                processor.process(descriptor);
            }
        }
    }

    /**
    * Publish a message composed of binary data stored in the given file. If the
    * client and server share the same file system, the absolute path of the file
    * is transfered to the server. Otherwise, the content of the file is transfered
    * to the server as the payload of a message.
    *
    * @param desc the message descriptor, which message id (mid) should not be null
    * @param file the file to be transfered
    */
    public void publishFile(Descriptor desc, File file) throws Exception
    {
        if (!this.sharedFileSystem) {
            byte[] buffer = loadData(file);
            this.publishBuffer(desc, buffer);
        } else {
            Pdu pdu = this.makePublishPdu(desc);
            pdu.putString("file", file.getAbsolutePath());
            this.channel.writeForReply(pdu, ackTimeout, "ok");
        }
    }

    /**
    * Publish a message composed of the given binary data.
    *
    * @param desc the message descriptor, which message id (mid) should not be null
    * @param buffer the payload to be transfered
    */
    public void publishBuffer(Descriptor desc, byte[] buffer) throws Exception
    {
        Pdu pdu = this.makePublishPdu(desc);
        pdu.putBinaryData("data", new BinaryData(buffer));
        this.channel.writeForReply(pdu, ackTimeout, "ok");
    }


    /**
    * Subscribe in order to receive messages which descriptors match the given pattern
    *
    * @param skey the subscription (unique) key
    * @param pattern a descriptor that contains values that should match the received messages
    * @param processor instance which processes received descriptors (which message ids should not be null)
    */
    public void addSubscription(String skey, Descriptor pattern, Processor<Descriptor> processor) throws Exception
    {
        this.processors.put(skey, processor);
        Pdu pdu = this.makeSubPdu(skey, pattern);
        this.channel.writeForReply(pdu, ackTimeout, "ok");
    }

    /**
    * Unregister a subscription
    *
    * @param the key of the subscription to be unregistered
    */
    public void removeSubscription(String skey) throws Exception
    {
        Pdu pdu = this.makeSubsPdu("remove_sub", new String[]{ skey });
        this.channel.writeForReply(pdu, ackTimeout, "ok");
        this.processors.remove(skey);
    }


    /**
    * Give all ids of messages currently in the server cache that match the
    * subscriptions having the given keys
    *
    * @param skeys subscription keys. If some keys are unknown, they are ignored
    * @return messages ids that match the given subscriptions (may be empty)
    */
    public Set<String> getMatching(Set<String> skeys) throws Exception
    {
        int i = 0;
        String[] skeyArray = new String[skeys.size()];
        for (String skey : skeys) {
            skeyArray[i++] = skey;
        }
        Pdu pdu = this.makeSubsPdu("get_matching", skeyArray);
        Pdu reply = this.channel.writeForReply(pdu, ackTimeout, "recv_mids");
        Set<String> midSet = new HashSet<>();
        String[] mids = reply.getStringArray("mids");
        if (mids != null) {
            for (String mid : mids) {
                midSet.add(mid);
            }
        }
        return midSet;
    }

    /**
    * Give the descriptor of the message having the given id
    *
    * @param mid a message id
    * @return the message descriptor
    */
    public Descriptor getDescriptor(String mid) throws Exception
    {
        Pdu pdu = this.makeMidPdu("get_desc", mid);
        Pdu reply = this.channel.writeForReply(pdu, ackTimeout, "recv_desc");
        Descriptor descriptor = new Descriptor(reply.getAttributes("desc"));
        descriptor.setMid(reply.getString("mid"));
        return descriptor;
    }

    /**
    * Copy the content of a message in a file. If the client and server share the
    * same file system, the absolute path of the file is transfered to the server
    * that directly copies the content of the message to the file. Otherwise,
    * the content of the file is transfered from the server as the payload of a message
    *
    * @param mid the requested message id
    * @param file the file that should contain the message content after this method
    */
    public void getAsFile(String mid, File file) throws Exception
    {
        if (!this.sharedFileSystem) {
            byte[] data = this.getAsBuffer(mid);
            this.saveData(data, file);
        } else {
            Pdu pdu = this.makeMidPdu("get_payload", mid);
            pdu.putString("file", file.getAbsolutePath());
            Pdu reply = this.channel.writeForReply(pdu, ackTimeout, "recv_payload");
        }
    }

    /**
    * Give the content of a message
    *
    * @param mid the requested message id
    * @return the message payload
    */
    public byte[] getAsBuffer(String mid) throws Exception
    {
        Pdu pdu = this.makeMidPdu("get_payload", mid);
        Pdu reply = this.channel.writeForReply(pdu, ackTimeout, "recv_payload");
        BinaryData data = reply.getBinaryData("data");
        if (data == null) {
            throw new UnexpectedPduException("Unexpected reply received from the server: payload missing", reply);
        }
        return data.buffer.array();
    }

    //------------------------------------------------------------------------
    // Files
    //------------------------------------------------------------------------
    private byte[] loadData(File file) throws Exception
    {
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            int size = (int)fileChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate(size);
            int read = fileChannel.read(buffer, 0);
            while (read < size) {
                read += fileChannel.read(buffer, read);
            }
            return buffer.array();
        }
    }

    private void saveData(byte[] data, File file) throws Exception
    {
        try (FileChannel fileChannel = new FileOutputStream(file).getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int written = fileChannel.write(buffer, 0);
            while (written < data.length) {
                written += fileChannel.write(buffer, written);
            }
        }
    }

    //------------------------------------------------------------------------
    // Control PDUs
    //------------------------------------------------------------------------
    private Pdu makeMidPdu(String name, String mid)
    {
        Pdu pdu = this.makePdu(name);
        pdu.putString("mid", mid);
        return pdu;
    }

    private Pdu makeSubPdu(String skey, Descriptor descriptor)
    {
        Pdu pdu = this.makePdu("add_sub");
        pdu.putString("key", skey);
        pdu.putAttributes("desc", descriptor.getAttributes());
        return pdu;
    }

    private Pdu makeSubsPdu(String name, String[] skeys)
    {
        Pdu pdu = this.makePdu(name);
        pdu.putStringArray("subs", skeys);
        return pdu;
    }

    private Pdu makePublishPdu(Descriptor descriptor)
    {
        Pdu pdu = this.makePdu("publish");
        pdu.putString("mid", descriptor.getMid());
        pdu.putAttributes("desc", descriptor.getAttributes());
        return pdu;
    }

    private Pdu makePdu(String name)
    {
        Pdu pdu = this.channel.makePdu();
        pdu.putString("tkn", "PUBSUB-" + TKN_CPT++);
        pdu.putString("name", name);
        return pdu;
    }
}
