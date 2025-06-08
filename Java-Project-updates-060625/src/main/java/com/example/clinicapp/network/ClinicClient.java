// package com.example.clinicapp.network;
package com.example.clinicapp.network;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ClinicClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<NetworkMessage> onMessageReceived;

    public ClinicClient(String serverAddress, int port, Consumer<NetworkMessage> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, port);
        // WAŻNE: Kolejność streamów
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.onMessageReceived = onMessageReceived;
    }

    public void sendMessage(NetworkMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(() -> {
            try {
                NetworkMessage fromServer;
                while ((fromServer = (NetworkMessage) in.readObject()) != null) {
                    onMessageReceived.accept(fromServer);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Połączenie z serwerem zerwane.");
            }
        }).start();
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}