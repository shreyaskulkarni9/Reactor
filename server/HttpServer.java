package server;
import java.io.*;
import java.net.*;

public class HttpServer {
    public ServerSocket serverSocket;
    private volatile boolean running = false;
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
}
