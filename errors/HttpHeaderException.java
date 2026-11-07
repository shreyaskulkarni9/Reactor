package errors;
public class HttpHeaderException extends HttpException {
    public HttpHeaderException(String message)
    {
        super(message,400);
    }
    
}
