package casa.util.dodwanclient;

import casa.util.pdu.Attributes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
* A descriptor that contains name,value pairs used to describe a message
*/
public class Descriptor
{
    private static final String MID_KEY = "_docid";

    /**
    * the name,value pairs
    */
    private final Attributes attr;

    /**
    * Constructor
    */
    public Descriptor()
    {
        this.attr = new Attributes();
    }

    /**
    * Constructor
    *
    * @param properties the name,value pairs
    */
    public Descriptor(Attributes attr)
    {
        if (attr != null) {
            this.attr = attr;
        } else {
            this.attr = new Attributes();
        }
    }

    /**
    * Give the name,value pairs of this descriptor
    *
    * @return name,value pairs
    */
    public Attributes getAttributes()
    {
        return this.attr;
    }

    /**
    * Give the (unique) id of the message
    *
    * @return the message id (may be null)
    */
    public String getMid()
    {
        return this.attr.get(MID_KEY);
    }

    /**
    * Set the (unique) id of the message
    *
    * @return the message id
    */
    public void setMid(String mid)
    {
        if (mid != null) {
            this.attr.put(MID_KEY, mid);
        } else {
            this.attr.remove(MID_KEY);
        }
    }

    /**
    * Give the value associated to the given name
    *
    * @param name a name
    * @return the value associated to the name (may be null)
    */
    public String getAttribute(String name)
    {
        return this.attr.get(name);
    }

    /**
    * Give the names of all pairs in this descriptor
    *
    * @return all the names
    */
    public Set<String> getNames()
    {
        return this.attr.keySet();
    }

    /**
    * Register a name,value pair. If the given name is already registered in this
    * descriptor with another value, the given value replaces the previous one
    *
    * @param name a name
    * @param value the value to be associated to the name
    */
    public void setAttribute(String name, String value)
    {
        if (value != null) {
            this.attr.put(name, value);
        } else {
            this.attr.remove(name);
        }
    }

    /**
    * Unregister a name,value pair. If the given name is not known, do nothing
    *
    * @param name the name to be unregistered
    */
    public void removeAttribute(String name)
    {
        this.attr.remove(name);
    }

    /**
    * Give a string representation of this descriptor
    *
    * @return string representation of this descriptor
    */
    public String toString()
    {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Properties props = new Properties();
            for (Map.Entry<String,String> entry : this.attr.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            props.store(out, "");
            return out.toString("UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /**
    * Give a descriptor instance from a string representation. The string
    * representation returned by the {@link toString()} method should give an
    * instance having the same values
    *
    * @param str string representation of a descriptor
    * @return a descriptor instance. Null if the given string format is not correct
    */
    public static Descriptor fromString(String str)
    {
        Descriptor descriptor = new Descriptor();
        try (ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"))) {
            Properties props = new Properties();
            props.load(in);
            for (String name : props.stringPropertyNames()) {
                descriptor.setAttribute(name, props.getProperty(name));
            }
        } catch (Exception e) {
            return null;
        }
        return descriptor;
    }
}
