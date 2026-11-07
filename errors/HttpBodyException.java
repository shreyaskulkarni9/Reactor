package errors;
public class HttpBodyException extends HttpException {
    public HttpBodyException(String message)
    {
        super(message,400);
    }
    public HttpBodyException(String message,int statusCode)
    {
        super(message,statusCode);
    }
}
