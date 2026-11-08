package http;

import java.util.*;
public class HttpRequest
{
    private final String httpMethod;
    private final String httpVersion;
    private final Map<String,List<String>> httpHeaders;
    private final String requestTarget;
    private final byte[] httpBody;
    public HttpRequest(String httpMethod,String httpVersion, String requestTarget,byte[] httpBody,Map<String,List<String>> httpHeaders)
    {
        this.httpMethod = httpMethod;
        this.httpVersion = httpVersion;
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet())
        {
            copy.put(entry.getKey(),Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.httpHeaders = Collections.unmodifiableMap(copy);
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
    public Map<String,List<String>> getHeaders()
    {
        return httpHeaders;
    }
    public byte[] getBody()
    {
        return httpBody.clone();
    }
}