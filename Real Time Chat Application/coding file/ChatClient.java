import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;  
    private PrintWriter out;    
    private BufferedReader userInput;  
    public ChatClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            userInput = new BufferedReader(new InputStreamReader(System.in));
            new Thread(new ServerListener()).start();
            while (true) {
                String message = userInput.readLine();  
                out.println(message); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String messageFromServer;
                while ((messageFromServer = in.readLine()) != null) {
                    System.out.println(messageFromServer);  
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; 
        int port = 12345;
        new ChatClient(serverAddress, port);   
        
    }
}
