package org.academiadecodigo.splicegirls;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private File file;
    private FileInputStream fileStream;
    private String filename = "";
    private DataOutputStream responseDataStream;
    private Socket browserSocket;

    public ClientHandler(Socket browserSocket){
        this.browserSocket = browserSocket;
    }

    @Override
    public void run() {
        System.out.println("Client connected. Port: " + browserSocket.getLocalPort() + " Address:" + browserSocket.getInetAddress().getHostAddress());

        int result = 0;
        try {
            result = handleRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == 1) {
            try {
                sendResponse(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                replyToBadRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            browserSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int handleRequest() throws IOException {

        BufferedReader requestStream = new BufferedReader(new InputStreamReader(browserSocket.getInputStream()));
        String requestStreamLine = requestStream.readLine();

        if (requestStreamLine==null || requestStreamLine.isEmpty()){
            System.err.println("Null or empty request from browser");
            return -1;
        }

        String[] requestLineWords = requestStreamLine.split(" ");
        String httpVerb = requestLineWords[0];

        if (!httpVerb.equals("GET")){
            System.err.println("Can only handle GET requests from browser");
            return -1;
        }

        filename = requestLineWords[1];

        System.out.println(requestStreamLine);
        return 1;
    }

    private void sendResponse(String filename) throws IOException {

        if (filename.equals("/")){
            String indexPath = "/index.html";
            filename = indexPath;
        }

        String resourceFilePath = "www" + filename;
        file = new File(resourceFilePath);

        if(!file.exists()){
            String html404Path = "/404.html";
            filename = html404Path;
            resourceFilePath = "www" + filename;
            file = new File(resourceFilePath);
        }

        String[] filenameWords = filename.split("\\.");
        String resourceFileExtension = filenameWords[1];

        fileStream = new FileInputStream(file);

        System.out.println("Delivering " + resourceFileExtension + " file  with " + file.length() + " bytes");

        String mimeType = "";

        if (resourceFileExtension.equals("html")){
            mimeType = "text";
        }

        if (resourceFileExtension.equals("png") || resourceFileExtension.equals("jpg") || resourceFileExtension.equals("gif") || resourceFileExtension.equals("ico")){
            mimeType = "image";
        }

        responseDataStream = new DataOutputStream (browserSocket.getOutputStream());

        responseDataStream.writeBytes("HTTP/1.0 200 Document Follows\nContent-Type: " + mimeType + "/"+ resourceFileExtension +"; charset=UTF-8\nContent-Length: " + file.length() + " bytes\n\n");
        responseDataStream.write(fileStream.readAllBytes());

        fileStream.close();
        responseDataStream.close();
        browserSocket.close();
        System.out.println("Closing socket for client " + browserSocket.getInetAddress().getHostAddress());
    }

    private void replyToBadRequest() throws IOException {

        String badRequestPath = "www/badrequest.html";
        file = new File(badRequestPath);

        fileStream = new FileInputStream(file);

        System.out.println("Delivering bad request html file with " + file.length() + " bytes");

        responseDataStream = new DataOutputStream (browserSocket.getOutputStream());

        responseDataStream.writeBytes("HTTP/1.0 200 Document Follows\nContent-Type: text/html; charset=UTF-8\nContent-Length: " + file.length() + " bytes\n\n");
        responseDataStream.write(fileStream.readAllBytes());

        fileStream.close();
        responseDataStream.close();
        browserSocket.close();
        System.out.println("Closing socket for client " + browserSocket.getInetAddress().getHostAddress());
    }
}
