package errors;

import errors.HttpException;

public class HttpRequestLineException extends HttpException{
    public HttpRequestLineException(String message)
    {
        super(message);
    }
}
