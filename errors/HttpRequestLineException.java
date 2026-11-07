package errors;

public class HttpRequestLineException extends HttpException{
    public HttpRequestLineException(String message)
    {
        super(message,400);
    }
}
