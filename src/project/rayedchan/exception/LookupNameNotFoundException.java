package project.rayedchan.exception;

/**
 * @author rayedchan
 * Exception for when a lookup does not exist in OIM.
 */
public class LookupNameNotFoundException extends Exception
{
    //Constructor that accepts a message
    public LookupNameNotFoundException()
    {
        super("Lookup Definition does not exist.");
    }
}
