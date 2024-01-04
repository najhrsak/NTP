package model;

import java.io.*;
import java.net.*;

public class ClientSocket {
    public String run(String username) {
        try {
            int serverPort = 31654;
            InetAddress host = InetAddress.getByName("localhost");
            System.out.println("Connecting to server on port " + serverPort);

            Socket socket = new Socket(host,serverPort);
            //Socket socket = new Socket("127.0.0.1", serverPort);
            System.out.println("Just connected to " + socket.getRemoteSocketAddress());
            PrintWriter toServer =
                    new PrintWriter(socket.getOutputStream(),true);
            toServer.println(username);
            toServer.close();
            socket.close();

            socket = new Socket(host,serverPort);
            BufferedReader fromServer =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            String line = fromServer.readLine();
            System.out.println("Client received: " + line + " from Server");
            fromServer.close();
            socket.close();



            return line;
        }
        catch(UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return "Error connecting to TCP server";
    }
}
