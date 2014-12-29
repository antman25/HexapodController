/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author ANTMAN
 */
public class TCPClient implements Runnable{

    public final static int NULL = 0;
    public final static int DISCONNECTED = 1;
    public final static int DISCONNECTING = 2;
    public final static int BEGIN_CONNECT = 3;
    public final static int CONNECTED = 4;
    
    private String host = "10.0.0.100";
    private int port = 5555;
    private int connectionStatus = DISCONNECTED;
    
    public Socket socket = null;
    public BufferedReader in = null;
    public PrintWriter out = null;
    
    
    public TCPClient()
    {
        
    }
    
    public TCPClient(String host, Integer port)
    {
        this.host = host;
        this.port = port;
    }
    
    public boolean isConnnected()
    {
        if (socket != null)
            return socket.isConnected();
        return false;
    }
    
    
    public void setHost(String host)
    {
        this.host = host;
    }
    
    public void setPort(Integer port)
    {
        this.port = port;
    }
    
    public String getHost()
    {
        return host;
    }
    
    public Integer getPort()
    {
        return port;
    }
    
    public void sendString(String s) {
        System.out.println("Sending: " + s);
        out.write(s + "\n");
        out.flush();
    }
    
    // Cleanup for disconnect
    public void cleanUp() {
        try 
        {
           if (socket != null) 
           {
              socket.close();
              socket = null;
           }
        }
        catch (IOException e) 
        { 
            socket = null; 
        }

        try 
        {
            if (in != null) 
            {
                in.close();
                in = null;
            }
        }
        catch (IOException e) 
        { 
            in = null; 
        }

        if (out != null) 
        {
            out.close();
            out = null;
        }
    }
    
    public Boolean connect()
    {
        try
        {
            connectionStatus = BEGIN_CONNECT;
            socket = new Socket(host, port);
            if (socket.isConnected())
                connectionStatus = CONNECTED;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            //out.write("S#1 P1300 T1000\r");
        }
        catch (IOException ex)
        {
            cleanUp();
            System.out.println(ex.toString());
        }
        catch (Exception ex)
        {
            System.out.println(ex.toString());
        }
        return true;
    }
    
    public void run() {
        
    }
    
}
