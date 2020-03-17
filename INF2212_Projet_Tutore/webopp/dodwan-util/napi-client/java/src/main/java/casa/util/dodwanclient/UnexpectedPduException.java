package casa.util.dodwanclient;

import casa.util.pdu.Pdu;

/**
* Exception that occurs when a PDU doesn't have the expected fields
*/
public class UnexpectedPduException extends Exception
{
    /**
    * The PDU source of the exception
    */
    private Pdu pdu;

    /**
    * Constructor
    *
    * @param message an appropriate error message
    * @param pdu the PDU source of the exception
    */
    public UnexpectedPduException(String message, Pdu pdu)
    {
        super(message);
    }

    /**
    * Give the PDU source of the exception
    *
    * @return the PDU
    */
    public Pdu getPdu()
    {
        return this.pdu;
    }
}
