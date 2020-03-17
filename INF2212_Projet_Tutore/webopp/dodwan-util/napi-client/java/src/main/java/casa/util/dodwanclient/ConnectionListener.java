package casa.util.dodwanclient;

public interface ConnectionListener
{
    /**
    * Method called when the connection is opened
    */
    void onConnectionOpened();

    /**
    * Method called when the connection is closed
    */
    void onConnectionClosed();

    /**
    * Method called when an error occurs while opening the connection
    */
    void onConnectionError(Exception e);
}
