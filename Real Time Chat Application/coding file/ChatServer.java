import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final int PORT = 12345;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String serverMessage = serverInput.readLine();
                    if (serverMessage != null && !serverMessage.isEmpty()) {
                        broadcast("[Server]: " + serverMessage, null);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();  
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (sender == null) {
                client.sendMessage(message);
            } else if (client == sender) {
                client.sendMessage("You: " + message);
            } else {
                client.sendMessage(sender.getClientName() + ": " + message);
            }
        }
    }

    public static synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            while (clientName == null || clientName.trim().isEmpty()) {
                out.println("Enter your name: ");
                clientName = in.readLine();
                
            }
            System.out.println(clientName + " has joined the chat.");
            ChatServer.broadcast(clientName + " has joined the chat!", this);
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(clientName + ": " + message);
                ChatServer.broadcast(message, this);  
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(clientName + " has left the chat.", this);
            System.out.println(clientName + " has left the chat.");
        }
    }
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getClientName() {
        return clientName;
    }
}
