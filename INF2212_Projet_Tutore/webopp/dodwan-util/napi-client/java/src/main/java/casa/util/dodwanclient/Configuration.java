package casa.util.dodwanclient;

import casa.util.pdu.BsonPduSerializer;
import casa.util.pdu.PduSerializer;
import java.util.Properties;

public class Configuration
{
    /**
    * The default server host name
    */
    private static final String SERVER_HOST = "localhost";

    /**
    * The default server port number
    */
    private static final int SERVER_PORT = 8030;

    /**
    * The default serializer used to exchange PDUs
    */
    private static final PduSerializer SERIALIZER = new BsonPduSerializer();

    /**
    * The default timeout for read operations
    */
    private static final int READ_TIMEOUT = 3000;

    /**
    * The default timeout for server replies
    */
    private static final int REPLY_TIMEOUT = 1000;

    /**
    * The default minimum delay between subsequent ping messages emissions
    */
    private static final int PING_PONG_DELAY = 1000;

    /**
    * The default minimum delay between subsequent TCP connection attempts
    */
    private static final int CONN_ATTEMPTS_DELAY = 1000;

    /**
    * The default maximum number of successive TCP connection attempts
    */
    private static final int MAX_CONN_ATTEMPTS = 5;

    /**
    * The default identifier of the client for the current session
    */
    private static final String CLIENT_ID = "clientId";

    /**
    * True if the client and server share the same file system
    */
    private static final boolean SHARED_FILE_SYSTEM = false;

    /**
    * Singleton instance of this class
    */
    private static Configuration instance;

    public static void init(Properties conf)
    {
        // TODO init configuration
    }

    /**
    * Give a singleton instance of this class
    *
    * @return singleton {@link Configuration}
    */
    public static Configuration getInstance()
    {
        if (instance == null) {
            instance = new Configuration();
        }
        return null;
    }

    /**
    * Private constructor (singleton pattern)
    */
    private Configuration() { }

    /**
    * Give the server host name
    *
    * @return the server host name
    */
    public String getServerHost()
    {
        return SERVER_HOST;
    }

    /**
    * Give the server port number
    *
    * @return the server port number
    */
    public int getServerPort()
    {
        return SERVER_PORT;
    }

    /**
    * Give the serializer used to exchange PDUs
    *
    * @return the serializer used to exchange PDUs
    */
    public PduSerializer getSerializer()
    {
        return SERIALIZER;
    }

    /**
    * Give the timeout for read operations
    *
    * @return the timeout for read operations
    */
    public int getReadTimeout()
    {
        return READ_TIMEOUT;
    }

    /**
    * Give the timeout for server replies
    *
    * @return the timeout for server replies
    */
    public int getReplyTimeout()
    {
        return REPLY_TIMEOUT;
    }

    /**
    * Give the minimum delay between subsequent ping messages emissions
    *
    * @return the minimum delay between subsequent ping messages emissions
    */
    public int getPingPongDelay()
    {
        return PING_PONG_DELAY;
    }

    /**
    * Give the minimum delay between subsequent TCP connection attempts
    *
    * @return the minimum delay between subsequent TCP connection attempts
    */
    public int getConnAttemptsDelay()
    {
        return CONN_ATTEMPTS_DELAY;
    }

    /**
    * Give the maximum number of successive TCP connection attempts
    *
    * @return the maximum number of successive TCP connection attempts
    */
    public int getMaxConnAttempts()
    {
        return MAX_CONN_ATTEMPTS;
    }

    /**
    * Give the identifier of the client for the current session
    *
    * @return the identifier of the client for the current session
    */
    public String getClientId()
    {
        return CLIENT_ID;
    }

    /**
    * Give true if the client and server share the same file system
    *
    * @return true if the client and server share the same file system
    */
    public boolean sharedFileSystem()
    {
        return SHARED_FILE_SYSTEM;
    }

}
