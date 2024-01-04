package model;


import java.net.*;
import java.net.ServerSocket;
import java.io.*;

public class ServerSideSocket {
    public void run() {
        try {
            int serverPort = 31654;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true) {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");

                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                BufferedReader fromClient =
                        new BufferedReader(
                                new InputStreamReader(server.getInputStream()));
                String username = fromClient.readLine();
                System.out.println("Server received: " + username);

                server.close();

                server = serverSocket.accept();

                PrintWriter toClient =
                        new PrintWriter(server.getOutputStream(),true);
                toClient.println("Hello " + username);

                /**/

            }
        }
        catch(UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServerSideSocket srv = new ServerSideSocket();
        srv.run();
    }
}
