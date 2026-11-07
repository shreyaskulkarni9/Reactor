package errors;
public class HttpException extends Exception 
{
    private int statusCode;
    public HttpException(String message,int statusCode)
    {
        super(message);
        this.statusCode = statusCode;
    }
    public int getStatusCode()
    {
        return statusCode;
    }
}
