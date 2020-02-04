package casa.util.dodwanclient;

/**
* A listener that observes the changes in the status of a session
*/
public interface SessionListener extends ConnectionListener
{
    /**
    * Method called when the session is being starting: trying to (re)open a connection
    * and transfer opening pdus to the server
    */
    void onSessionStarting();

    /**
    * Method called when the session is started
    */
    void onSessionStarted();

    /**
    * Method called when the session is stopped. Can occur either after a
    * {@link Session#stop()} invocation or when the connection cannot be established
    * or reestablished after having been disconnected
    */
    void onSessionStopped();
}
