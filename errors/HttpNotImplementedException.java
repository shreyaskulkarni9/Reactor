package errors;

public class HttpNotImplementedException extends HttpException {

    public HttpNotImplementedException(String message) {
        super(message, 501);
    }
}
