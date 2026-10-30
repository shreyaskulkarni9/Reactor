import java.util.*;
public class HttpResponse {
    private final String httpVersion;
    private final String statusCode;
    private final String reasonPhrase;
    private final Map<String,String> httpHeaders;
    private final byte[] httpBody;
    public HttpResponse(String httpVersion,String statusCode,String reasonPhrase,Map<String,String> httpHeaders,byte[] httpBody) {
        this.httpVersion = httpVersion;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.httpHeaders = Collections.unmodifiableMap(new HashMap<>(httpHeaders));
        this.httpBody = httpBody.clone();
    }
    public String getVersion()
    {
        return httpVersion;
    }
    public String getStatusCode()
    {
        return statusCode;
    }
    public String getReasonPhrase()
    {
        return reasonPhrase;
    }
    public Map<String,String> getHttpHeaders()
    {
        return httpHeaders;
    }
    public byte[] getHttpBody()
    {
        return httpBody.clone();
    }
}
