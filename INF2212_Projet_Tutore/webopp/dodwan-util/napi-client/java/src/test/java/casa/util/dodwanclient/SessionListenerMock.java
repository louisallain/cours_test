package casa.util.dodwanclient;

import casa.util.Future;
import static org.junit.Assert.*;

/**
* A session listener that allows to spy the events fired. Initializes with a list
* of expected events and a lock object (to unlock when an unexpected event or the last event occur)
*/
class SessionListenerMock implements SessionListener
{
    private static final int CONN_OPENED = 0;
    private static final int CONN_CLOSED = 1;
    private static final int CONN_ERR = 2;
    private static final int SESS_STING = 3;
    private static final int SESS_STED = 4;
    private static final int SESS_STOP = 5;

    private final Future<Boolean> lock;  // lock object
    private final int[] expected;        // the expected events
    private int current;                 // the index of the event that should occur

    /**
    * Constructor
    * @param expected the expected events (that should occur)
    */
    SessionListenerMock(int[] expected)
    {
        this.lock = new Future<>(1000, false);
        this.expected = expected;
    }

    /**
    * Wait for unlock and check that the last event occured
    */
    public void checkEvents()
    {
        assertTrue("All expected events didn't occur", lock.getValue());
    }

    @Override
    public void onConnectionOpened() { this.event(CONN_OPENED); }

    @Override
    public void onConnectionClosed() { this.event(CONN_CLOSED); }

    @Override
    public void onConnectionError(Exception e) { this.event(CONN_ERR); }

    @Override
    public void onSessionStarting() { this.event(SESS_STING); }

    @Override
    public void onSessionStarted() { this.event(SESS_STED); }

    @Override
    public void onSessionStopped() { this.event(SESS_STOP); }

    /**
    * Check that the given event is expected and unlocks if it is the last expected event
    */
    private void event(int i) {
        if (current < expected.length) {
            assertEquals("Unexpected event occured", expected[current++], i);
        }
        if (current >= expected.length) {
            this.lock.putValue(true);
        }
    }
}
