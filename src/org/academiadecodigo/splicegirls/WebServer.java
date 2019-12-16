package org.academiadecodigo.splicegirls;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    private final int PORT = 8090;
    private Socket browserSocket;

    public void start(){

        try {
            connect();
            System.out.println("**" + Thread.currentThread().getName());

        } catch (PortUnreachableException e){
            System.err.println(e.getMessage());
        } catch (IOException e){
            System.err.println(e.getMessage());
        }
    }

    private void connect() throws IOException, PortUnreachableException {

        ServerSocket webServerSocket = new ServerSocket(PORT);
        System.out.println("WebServer listening for connections...");

        while(true) {
            Thread thread = new Thread(new ClientHandler(webServerSocket.accept()));
            thread.start();
            System.out.println("** " + thread.getName());
        }
    }
}