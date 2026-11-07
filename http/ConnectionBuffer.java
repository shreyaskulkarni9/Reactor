package http;
import errors.HttpBodyException;
import java.io.*;
import java.util.Arrays;
public class ConnectionBuffer {
    private static final int MAX_HEADER_SIZE = 16*1024;
    private static final int READ_BUFFER_SIZE = 1024;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private static final long MAX_BODY_SIZE = 1024 * 1024; // 1 MB
    public void readMore(InputStream input) throws IOException
    {
        byte[] temp = new byte[READ_BUFFER_SIZE];
        int bytesRead = input.read(temp);
        if(bytesRead ==-1) throw new EOFException("End of stream reached");
        if(bytesRead == 0) return;
        buffer.write(temp, 0, bytesRead);
    }
    public int findHeaderEnd()
    {
        byte[] data = buffer.toByteArray();
        for(int i=0; i<data.length-3; i++)
        {
            if(data[i] == 13 && data[i+1] == 10 && data[i+2] == 13 && data[i+3] == 10)
            {
                return i+4;
            }
        }
        return -1;
    }
    public int readHeaders(InputStream input) throws IOException
    {
        while(true)
        {
            int headerEnd = findHeaderEnd();
            if(headerEnd != -1)
            {
                byte[] headers = getHeaders(headerEnd);
                if(headers.length > MAX_HEADER_SIZE)
                {
                    throw new IOException("Header size exceeds maximum limit");
                }
                return headerEnd;
            }
            if(buffer.size() >= MAX_HEADER_SIZE)
            {
                throw new IOException("Header size exceeds maximum limit");
            }
            readMore(input);
        }
    }
    public byte[] getHeaders(int headerEnd)
    {
        byte[] data = buffer.toByteArray();
        return Arrays.copyOfRange(data, 0, headerEnd);
    }
    public byte[] getBody(int headerEnd)
    {
        byte[] data = buffer.toByteArray();
        return Arrays.copyOfRange(data, headerEnd, data.length);
    }
    public void consumeHeaders(int headerEnd)
    {
        byte[] remaining = getBody(headerEnd);

        buffer.reset();
        buffer.write(remaining, 0, remaining.length);
    }
    public byte[] readBody(int headerEnd, InputStream input,int contentLength) throws HttpBodyException,IOException
    {
        if(headerEnd < 0 || headerEnd > buffer.size())throw new IOException("Invalid header boundary");
        if(contentLength < 0) throw new IOException("Invalid content length");
        if(contentLength > MAX_BODY_SIZE) throw new HttpBodyException("Content length exceeds maximum limit",413);
        while(buffer.size() - headerEnd < contentLength)
        {
            try{readMore(input);}
            catch(EOFException e)
            {
                throw new HttpBodyException("Unexpected end of stream while reading body",400);
            }
        }
        byte[] body = buffer.toByteArray();
        return Arrays.copyOfRange(body, headerEnd, headerEnd + contentLength);
    }
}
