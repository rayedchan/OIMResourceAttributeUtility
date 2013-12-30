package project.rayedchan.exception;

/**
 * @author rayedchan
 * Exception for when file format is invalid.
 */
public class BadFileFormatException extends Exception
{
    public BadFileFormatException(String message)
    {
        super(message);
    }
}
