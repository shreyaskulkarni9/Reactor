package http;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ResponseWriter {

    public byte[] serialize(HttpResponse response) {
        String statusLine = response.getVersion() + " "
                + response.getStatusCode() + " "
                + response.getReasonPhrase() + "\r\n";
        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, String> entry : response.getHttpHeaders().entrySet()) {
            headers.append(entry.getKey())
                   .append(": ")
                   .append(entry.getValue())
                   .append("\r\n");
        }
        headers.append("\r\n");
        String responseHeaders = statusLine + headers;
        byte[] body = response.getHttpBody();
        byte[] respHeaders = responseHeaders.getBytes(StandardCharsets.ISO_8859_1);
        ByteArrayOutputStream output = new ByteArrayOutputStream(body.length + respHeaders.length);
        try {
            output.write(respHeaders);
            output.write(body);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize response", e);
        }
        return output.toByteArray();
    }
}