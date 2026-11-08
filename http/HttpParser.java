package http;
import errors.*;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class HttpParser
{
    private String method;
    private String target;
    private String version;
    private int requestLineEnd;
    private int headStart;
    private int headEnd;
    private int bodyStart;
    private String completeRequestLine;
    private String completeHeaders;
    public static long MAX_BODY_SIZE = 1024*1024;
    private Map<String, List<String>> completeHeadersMap = new HashMap<>();
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
        if(headStart >= 0 && headStart + 1 < data.length && data[headStart] == 13 && data[headStart + 1] == 10)
        {
            headEnd = headStart;
            bodyStart = headStart + 2;
            return;
        }
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
    public void extractAll(byte[] data) throws HttpHeaderException,HttpRequestLineException
    {
        requestLineBytes(data);
        if(requestLineEnd==-1) throw new HttpRequestLineException("Request Line is Incomplete");

        headerBytes(data);
        if(bodyStart==-1) throw new HttpHeaderException("Headers are Incomplete");

        byte[] requestLine = Arrays.copyOfRange(data, 0, requestLineEnd);
        byte[] headers = Arrays.copyOfRange(data, headStart, headEnd);

        completeRequestLine = decodeRequestLine(requestLine);
        completeHeaders = decodeHeader(headers);
    }
    public boolean isValidMethodToken(String method) {
        return method != null && method.matches("^[!#$%&'*+.^_`|~0-9a-zA-Z-]+$");
    }

    public boolean isMethodSupported(String method) {
        return method.equals("GET") ||
            method.equals("POST") ||
            method.equals("PUT") ||
            method.equals("DELETE") ||
            method.equals("HEAD") ||
            method.equals("OPTIONS") ||
            method.equals("PATCH");
    }
    public boolean isVersionCorrect(String version)
    {
        if(version.matches("HTTP/[0-9]\\.[0-9]"))
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
    public void parseRequestLine() throws HttpRequestLineException,HttpNotImplementedException
    {
        if (completeRequestLine == null || completeRequestLine.isEmpty()) throw new HttpRequestLineException("Request Line is Missing");
        for(int i=0;i<completeRequestLine.length();i++)
        {
            char c = completeRequestLine.charAt(i);
            if(c<=31 || c==127) throw new HttpRequestLineException("Invalid Character in Request Line");
        }
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
        if (!isValidMethodToken(method))throw new HttpRequestLineException("Invalid HTTP method token");
        if (!isMethodSupported(method)) throw new HttpNotImplementedException("HTTP method not supported");
        if(!isVersionCorrect(version)) throw new HttpRequestLineException("Invalid Version");
        if(version.equals("HTTP/1.0") || version.equals("HTTP/1.1")) {}
        else throw new HttpRequestLineException("Unsupported Version");
        if(!isTargetCorrect(target)) throw new HttpRequestLineException("Invalid Target");
    } 
    public boolean isValidName(String name)
    {
       return name.matches("^[!#$%&'*+.^_`|~0-9a-zA-Z-]+$");
    }
    public boolean isValidValue(String value)
    {
        return value.matches("^[\\x09\\x20-\\x7E\\x80-\\xFF]*$");
    }
    public boolean isValidHost(String value)
    {
        if(value == null || value.isEmpty()) return false;
        String hostname;
        String port = null;
        if(value.startsWith("["))
        {
            int closingBracket = value.indexOf(']');
            if(closingBracket == -1) return false;
            hostname = value.substring(1, closingBracket);
            String remaining = value.substring(closingBracket + 1);
            if(!remaining.isEmpty())
            {
                if(!remaining.startsWith(":")) return false;
                port = remaining.substring(1);
            }
            if(hostname.isEmpty() || !hostname.contains(":")) return false;
            try
            {
                InetAddress address = InetAddress.getByName(hostname);
                if(!(address instanceof Inet6Address)) return false;
            }
            catch(Exception e)
            {
                return false;
            }
        }
        else
        {
            int colonIndex = value.indexOf(':');
            if(colonIndex != -1)
            {
                if(colonIndex != value.lastIndexOf(':')) return false;
                hostname = value.substring(0, colonIndex);
                port = value.substring(colonIndex + 1);
            }
            else
            {
                hostname = value;
            }
            if(hostname.isEmpty()) return false;
            if(hostname.matches("[0-9.]+"))
            {
                String[] octets = hostname.split("\\.", -1);
                if(octets.length != 4) return false;
                for(String octet : octets)
                {
                    if(octet.isEmpty() || !octet.matches("[0-9]{1,3}"))
                        return false;
                    int number = Integer.parseInt(octet);
                    if(number < 0 || number > 255) return false;
                }
            }
            else
            {
                if(hostname.length() > 253) return false;
                String[] labels = hostname.split("\\.", -1);
                for(String label : labels)
                {
                    if(label.isEmpty() || label.length() > 63) return false;
                    if(!label.matches("[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?"))
                        return false;
                }
            }
        }
        if(port != null)
        {
            if(port.isEmpty() || !port.matches("[0-9]+")) return false;
            try
            {
                int portNumber = Integer.parseInt(port);
                if(portNumber < 1 || portNumber > 65535) return false;
            }
            catch(NumberFormatException e)
            {
                return false;
            }
        }
        return true;
    }
    public void parseHeaders() throws HttpHeaderException
    {
        completeHeadersMap.clear();
        if (completeHeaders == null) throw new HttpHeaderException("Headers Not Initialized");
        if(completeHeaders.isEmpty())
        {
            if(version.equals("HTTP/1.1")) throw new HttpHeaderException("Missing Host Header");
            return;
        }
        String[] headerLines = completeHeaders.split("\r\n");
        boolean containsHost = false;
        for (String line : headerLines)
        {
            if (line.startsWith(" ") || line.startsWith("\t")) throw new HttpHeaderException("Obsolete Header Folding");
            if (!line.contains(":")) throw new HttpHeaderException("Invalid Header Format");
            String[] parts = line.split(":", 2);
            String name = parts[0];
            String value = parts[1].replaceAll("^[ \\t]+|[ \\t]+$", "");
            if (name.isEmpty() || !isValidName(name)) throw new HttpHeaderException("Invalid Header Name");
            if(!isValidValue(value)) throw new HttpHeaderException("Invalid Header Value");
            name = name.toLowerCase(Locale.ROOT);
            if(name.equals("host"))
            {
                if(!isValidHost(value)) throw new HttpHeaderException("Host Header Value is Empty");
                containsHost = true;
            }
            if(completeHeadersMap.containsKey(name))
            {
                if(name.equals("host")) throw new HttpHeaderException("Multiple Host Headers");
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
        if(!containsHost && version.equals("HTTP/1.1")) throw new HttpHeaderException("Missing Host Header");
    }
    public int getContentLength() throws HttpHeaderException,HttpBodyException
    {
        validateMessageFraming();
        List<String> values = completeHeadersMap.get("content-length");
        if(values==null) return 0;
        if(values.size()!=1) throw new HttpHeaderException("Multiple Content-Length Headers");
        String value = values.get(0);
        if(value.isEmpty() || !value.matches("[0-9]+")) throw new HttpHeaderException("Invalid Content-Length Value");
        long contentLength = 0;
        try
        {
            contentLength = Long.parseLong(value);
        }
        catch(NumberFormatException e)
        {
            throw new HttpHeaderException("Invalid Content-Length Value");
        }
        if(contentLength > MAX_BODY_SIZE) throw new HttpBodyException("Content-Length Exceeds Maximum Limit",413);
        return (int)contentLength;
    }
    public void validateMessageFraming() throws HttpHeaderException {
        System.out.println("Headers: " + completeHeadersMap);
        System.out.println(
            "Transfer-Encoding exists: " +
            (completeHeadersMap.get("transfer-encoding") != null)
        );
        System.out.println(
            "Transfer-Encoding count: " +
            (completeHeadersMap.get("transfer-encoding") == null
                ? 0
                : completeHeadersMap.get("transfer-encoding").size())
        );
        List<String> transferEncoding = completeHeadersMap.get("transfer-encoding");
        List<String> contentLength = completeHeadersMap.get("content-length");
        if (transferEncoding != null && contentLength != null) {
            throw new HttpHeaderException(
                "Conflicting Transfer-Encoding and Content-Length"
            );
        }
        if (transferEncoding != null && transferEncoding.size() != 1) {
            throw new HttpHeaderException(
                "Multiple Transfer-Encoding Headers"
            );
        }
        if (transferEncoding != null) {
            throw new HttpHeaderException(
                "Transfer-Encoding is not supported"
            );
        }
    }
    public String getMethod()
    { 
        return method;
    }
    public String getVersion()
    {
        return version;
    }
    public String getTarget()
    {
        return target;
    }
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry :completeHeadersMap.entrySet())
        {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}