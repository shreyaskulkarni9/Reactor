package errors;
import errors.HttpException;
public class HttpHeaderException extends HttpException {
    public HttpHeaderException(String message)
    {
        super(message);
    }
    
}
