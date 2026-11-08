package server;
import errors.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import http.*;
public class HttpServer {
    public ServerSocket serverSocket;
    private volatile boolean running = false;
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.start(8080);
    }
    public void start(int port)
    {
        try{
            serverSocket = new ServerSocket(port);
            running = true;
            try
            {
                while(running)
                {
                    Socket server_socket = serverSocket.accept();
                    processClient(server_socket);
                }
            }
            catch(SocketException e)
            {
                if(running)
                {
                    throw e;
                }
            }
            finally
            {
                running = false;
                if (!serverSocket.isClosed()) 
                {
                    serverSocket.close();
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public void processClient(Socket server_socket)
    {
        try(Socket server = server_socket)
        {
            InputStream input = server.getInputStream();
            ConnectionBuffer connectionBuffer = new ConnectionBuffer();
            int headerEnd = connectionBuffer.readHeaders(input);
            HttpParser parser = new HttpParser();
            byte[] headerBytes = connectionBuffer.getHeaders(headerEnd);
            try
            {
                parser.extractAll(headerBytes);
                parser.parseRequestLine();
                parser.parseHeaders();
                parser.validateMessageFraming();
                int contentLength = parser.getContentLength();
                byte[] body = connectionBuffer.readBody(headerEnd, input, contentLength);
                HttpRequest request = new HttpRequest(parser.getMethod(),parser.getVersion(),parser.getTarget(),body,parser.getHeaders());
                byte[] responseBody = "Request received!".getBytes(StandardCharsets.UTF_8);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "text/plain; charset=utf-8");
                headers.put("Content-Length", String.valueOf(responseBody.length));
                headers.put("Connection", "close");
                HttpResponse response = new HttpResponse(
                    "HTTP/1.1",
                    200,
                    "OK",
                    headers,
                    responseBody
                );
                ResponseWriter writer = new ResponseWriter();
                byte[] responseBytes = writer.serialize(response);
                OutputStream output = server.getOutputStream();
                output.write(responseBytes);
                output.flush();
            }
            catch(HttpException e)
            {
                 System.err.println("HTTP Error " + e.getStatusCode() + ": " + e.getMessage());
                try {
                    sendErrorResponse(server, e.getStatusCode());
                } catch(IOException ioException) {
                    System.err.println("Failed to send HTTP error response.");
                }
                return;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    public void stop() throws IOException
    {
        running = false;
        if(serverSocket!=null && !serverSocket.isClosed())
        {
            serverSocket.close();
        }
    }
    private void sendErrorResponse(Socket server, int statusCode) throws IOException {
        String reasonPhrase;

        switch (statusCode) {
            case 400:
                reasonPhrase = "Bad Request";
                break;
            case 413:
                reasonPhrase = "Content Too Large";
                break;
            case 501:
                reasonPhrase = "Not Implemented";
                break;
            default:
                statusCode = 500;
                reasonPhrase = "Internal Server Error";
        }

        byte[] body = (reasonPhrase + "\n").getBytes(StandardCharsets.UTF_8);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain; charset=utf-8");
        headers.put("Content-Length", String.valueOf(body.length));
        headers.put("Connection", "close");

        HttpResponse response = new HttpResponse(
            "HTTP/1.1",
            statusCode,
            reasonPhrase,
            headers,
            body
        );
        ResponseWriter writer = new ResponseWriter();
        byte[] responseBytes = writer.serialize(response);
        OutputStream output = server.getOutputStream();
        output.write(responseBytes);
        output.flush();
    }
}
