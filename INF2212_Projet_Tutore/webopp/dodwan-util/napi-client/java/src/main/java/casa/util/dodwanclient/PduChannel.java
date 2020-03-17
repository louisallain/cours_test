package casa.util.dodwanclient;

import casa.util.Processor;
import casa.util.pdu.BinaryData;
import casa.util.pdu.Pdu;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* A channel used to send/receive requests/replies or receive notifications
*/
abstract class PduChannel
{
    /**
    * Processors for PDUs received from the server, associated to the "name" field of the expected PDUs
    */
    private final Map<String, Processor<Pdu>> processors;

    /**
    * Start receiving PDUs (replies or notifications).
    */
    public abstract void open();

    /**
    * Stop receiving PDUs.
    */
    public abstract void close();

    /**
    * Write a PDU through the connection. The channel should be opened
    *
    * @param pdu the PDU to be written
    */
    public abstract void write(Pdu pdu) throws Exception;

    /**
    * Write a PDU having a token through the connection and wait for a PDU from
    * the server having the same token. The channel should be opened
    *
    * @param pdu the PDU to be written
    * @param replyTimeout the timeout to wait for the reply
    * @return the received PDU. May be null, if the timeout expires
    */
    public abstract Pdu writeForReply(Pdu pdu, long replyTimeout) throws Exception;

    /**
    * Write a PDU having a token through the connection and wait for a PDU from
    * the server having the same token. The channel should be opened
    *
    * @param pdu the PDU to be written
    * @param replyTimeout the timeout to wait for the reply
    * @param expectedReply the expected name of the reply PDU
    * @return the received PDU. May be null, if the timeout expires
    *
    * @throws UnexpectedPduException if the received PDU doesn't have the expected name
    */
    public Pdu writeForReply(Pdu pdu, long replyTimeout, String expectedReply) throws UnexpectedPduException, Exception
    {
        Pdu reply = writeForReply(pdu, replyTimeout);
        this.checkReply(reply, expectedReply);
        return reply;
    }

    PduChannel()
    {
        this.processors = new ConcurrentHashMap<>();
    }

    /**
    * Create an empty PDU specialized for the Dodwan NAPI
    *
    * @return new PDU instance
    */
    public Pdu makePdu()
    {
        return new DodwanPdu();
    }

    /**
    * Register a new processor to process PDUs having the given name
    *
    * @param name the name of the PDUs to be processed
    * @param processor the processor to be registered
    */
    public void putProcessor(String name, Processor<Pdu> processor)
    {
        this.processors.put(name, processor);
    }

    /**
    * Unregister a PDU processor
    *
    * @param name the name of the processor to be unregistered
    */
    public void removeProcessor(String name)
    {
        this.processors.remove(name);
    }

    /**
    * Give a registered processor
    *
    * @param name the name of the processor
    * @return the processor
    */
    public Processor<Pdu> getProcessor(String name)
    {
        if (name != null) {
            return this.processors.get(name);
        }
        return null;
    }

    private void checkReply(Pdu pdu, String expectedName) throws UnexpectedPduException
    {
        String errorMessage = "No reply received from the server";
        if (pdu != null) {
            errorMessage = "Unexpected reply received from the server: PDU with no name field";
            String name = pdu.getString("name");
            if (name != null) {
                errorMessage = "Unexpected reply received from the server: " + name;
                if (name.equals(expectedName)) {
                    return;
                } else if ("error".equals(name)) {
                    errorMessage = "Server error";
                }
            }
        }
        throw new UnexpectedPduException(errorMessage, pdu);
    }
}

/**
* A PDU class specialized for the Dodwan NAPI
*/
class DodwanPdu extends Pdu
{

    // The association between a pdu key and the type of the corresponding value
    static final private Map <String, Class> pduTypes = new HashMap<String, Class>();
    static {
        pduTypes.put("name", String.class);
        pduTypes.put("client", String.class);
        pduTypes.put("dir", String.class);
        pduTypes.put("file", String.class);
        pduTypes.put("mid", String.class);
        pduTypes.put("key", String.class);
        pduTypes.put("tkn", String.class);
        pduTypes.put("pid", String.class);
        pduTypes.put("reason", String.class);
        pduTypes.put("ferror", String.class);

        pduTypes.put("cont", boolean.class);
        pduTypes.put("expired", boolean.class);
        pduTypes.put("dummy", int.class);
        pduTypes.put("expire", long.class);
        pduTypes.put("data", BinaryData.class);
        pduTypes.put("desc", Descriptor.class);
        pduTypes.put("subs", String[].class);
        pduTypes.put("pids", String[].class);
        pduTypes.put("mids", String[].class);
    }

    /**
    * Constructor. Initializes the specific Dodwan NAPI pdu types
    */
    @Override
    public Map <String, Class> pduTypes()
    {
        return pduTypes;
    }
}
