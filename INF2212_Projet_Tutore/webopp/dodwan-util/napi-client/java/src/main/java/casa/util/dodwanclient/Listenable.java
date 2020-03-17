package casa.util.dodwanclient;

import java.util.HashSet;
import java.util.Set;

/**
* A class representing objects observed by listeners
*
* @param T the type of listeners
*/
public class Listenable<T>
{
    /**
    * The registered listeners
    */
    private final Set<T> listeners;

    /**
    * Constructor
    */
    public Listenable()
    {
        this.listeners = new HashSet<>();
    }

    /**
    * Register a new listener to observe this instance
    *
    * @param listener the listener to be registered
    */
    public void addListener(T listener)
    {
        this.listeners.add(listener);
    }

    /**
    * Unregister a listener
    *
    * @param listener the listener to be unregistered
    */
    public void removeListener(T listener)
    {
        this.listeners.remove(listener);
    }

    /**
    * Give the registered listeners
    *
    * @return the listeners
    */
    public Set<T> getListeners()
    {
        return this.listeners;
    }
}
