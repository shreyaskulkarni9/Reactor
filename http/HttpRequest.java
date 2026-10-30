import java.util.*;
public class HttpRequest
{
    private final String httpMethod;
    private final String httpVersion;
    private final Map<String,String> httpHeaders;
    private final String requestTarget;
    private final byte[] httpBody;
    public HttpRequest(String httpMethod,String httpVersion, String requestTarget,byte[] httpBody,Map<String,String> httpHeaders)
    {
        this.httpMethod = httpMethod;
        this.httpVersion = httpVersion;
        this.httpHeaders = Collections.unmodifiableMap(new HashMap<>(httpHeaders));
        this.requestTarget = requestTarget;
        this.httpBody = httpBody.clone();
    }
    public String getMethod()
    {
        return httpMethod;
    }
    public String getVersion()
    {
        return httpVersion;
    }
    public String getRequestTarget()
    {
        return requestTarget;
    }
    public Map<String,String> getHeaders()
    {
        return httpHeaders;
    }
    public byte[] getBody()
    {
        return httpBody.clone();
    }
}