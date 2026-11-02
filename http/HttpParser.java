package http;
import errors.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class HttpParser
{
    private int requestLineEnd;
    private int headStart;
    private int headEnd;
    private int bodyStart;
    private String completeRequestLine;
    private String completeHeaders;
    public String decodeHeader(byte[] headers)
    {
        return new String(headers,StandardCharsets.ISO_8859_1);
    }
    public String decodeRequestLine(byte[] requestline)
    {
        return new String(requestline,StandardCharsets.US_ASCII);
    }
    public void requestLineBytes(byte[] data)
    {
        requestLineEnd = -1;
        headStart = -1;
        for(int i=0;i<data.length-1;i++)
        {
            if(data[i]==13 && data[i+1]==10)
            {
                requestLineEnd = i;
                headStart = i+2;
                return;
            }
        }
    }
    public void headerBytes(byte[] data)
    {
        headEnd = -1;
        bodyStart = -1;
        for(int i=headStart;i<data.length-3;i++)
        {
            if(data[i]==13 && data[i+1]==10 && data[i+2]==13 && data[i+3]==10)
            {
                bodyStart = i+4;
                headEnd = i;
                return;
            }
        }
    }
    public void extractAll(byte[] data) throws IllegalArgumentException
    {
        requestLineBytes(data);
        if(requestLineEnd==-1) throw new IllegalArgumentException("Request Line is Incomplete");

        headerBytes(data);
        if(bodyStart==-1) throw new IllegalArgumentException("Headera are Incomplete");

        byte[] requestLine = Arrays.copyOfRange(data, 0, requestLineEnd);
        byte[] headers = Arrays.copyOfRange(data, headStart, headEnd);

        completeRequestLine = decodeRequestLine(requestLine);
        completeHeaders = decodeHeader(headers);
    }
    private String method;
    private String target;
    private String version;
    public boolean isMethodCorrect(String method)
    {
        if(method.equals("GET") || method.equals("POST") || method.equals("PUT") || method.equals("DELETE") || method.equals("HEAD") || method.equals("OPTIONS") || method.equals("PATCH"))
        {
            return true;
        }
        return false;
    }
    public boolean isVersionCorrect(String version)
    {
        if(version.equals("HTTP/1.0") || version.equals("HTTP/1.1"))
        {
            return true;
        }
        return false;
    }
    public boolean isTargetCorrect(String target)
    {
        if (target == null || target.isEmpty() || target.charAt(0) != '/') return false;
        for (int i = 0; i < target.length(); i++)
        {
            char c = target.charAt(i);
            if (c <= 31 || c == 127) return false;
        }
        return true;
    }
    public void parseRequestLine() throws HttpRequestLineException
    {
        if (completeRequestLine == null || completeRequestLine.isEmpty()) throw new HttpRequestLineException("Request Line is Missing");
        int count = 0;
        for(int i=0;i<completeRequestLine.length();i++)
        {
            if(completeRequestLine.charAt(i)==' ') count++;
        }
        if(count!=2) throw new HttpRequestLineException("Improper Request Line");
        int firstSpace = completeRequestLine.indexOf(' ');
        int secondSpace = completeRequestLine.indexOf(' ', firstSpace + 1);
        method = completeRequestLine.substring(0, firstSpace);
        target = completeRequestLine.substring(firstSpace + 1, secondSpace);
        version = completeRequestLine.substring(secondSpace + 1);
        if(!isMethodCorrect(method)) throw new HttpRequestLineException("Invalid Method");
        if(!isVersionCorrect(version)) throw new HttpRequestLineException("Invalid Version");
        if(!isTargetCorrect(target)) throw new HttpRequestLineException("Invalid Target");
    } 
    private Map<String, List<String>> completeHeadersMap = new HashMap<>();
    public boolean isValidName(String name)
    {
       return name.matches("^[!#$%&'*+.^_`|~0-9a-zA-Z-]+$");
    }
    public boolean isValidValue(String value)
    {
        return value.matches("^[\\x09\\x20-\\x7E\\x80-\\xFF]*$");
    }
    public void parseHeaders() throws HttpHeaderException
    {
        completeHeadersMap.clear();
        if (completeHeaders == null) throw new HttpHeaderException("Headers Not Initialized");
        if(completeHeaders.isEmpty()) return;
        String[] headerLines = completeHeaders.split("\r\n");
        for (String line : headerLines)
        {
            if (line.startsWith(" ") || line.startsWith("\t")) throw new HttpHeaderException("Obsolete Header Folding");
            if (!line.contains(":")) throw new HttpHeaderException("Invalid Header Format");
            String[] parts = line.split(":", 2);
            String name = parts[0];
            String value = parts[1].replaceAll("^[ \\t]+|[ \\t]+$", "");
            if (name.isEmpty() || !isValidName(name)) throw new HttpHeaderException("Invalid Header Name or Value");
            if(!isValidValue(value)) throw new HttpHeaderException("Invalid Header Value");
            name = name.toLowerCase(Locale.ROOT);
            if(completeHeadersMap.containsKey(name))
            {
                List<String> values = completeHeadersMap.get(name);
                values.add(value);
                completeHeadersMap.put(name, values);
            }
            else
            {
                List<String> values = new ArrayList<>();
                values.add(value);
                completeHeadersMap.put(name, values);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        HttpParser parser = new HttpParser();

        String headers =
                "Host: example.com\r\n" +
                "X-Test: first\r\n" +
                "x-test: second";

        parser.completeHeaders = headers;

        parser.parseHeaders();

        System.out.println(parser.completeHeadersMap);
    }
}