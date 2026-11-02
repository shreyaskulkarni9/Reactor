package errors;
import errors.HttpException;
public class HttpBodyException extends HttpException {
    public HttpBodyException(String message)
    {
        super(message);
    }   
}
