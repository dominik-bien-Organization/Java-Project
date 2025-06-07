// package com.example.clinicapp.network;
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ClinicClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived; // Callback do przekazywania wiadomości do GUI

    public ClinicClient(String serverAddress, int port, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.onMessageReceived = onMessageReceived;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void startListening() {
        // Nasłuchiwanie w osobnym wątku, aby nie blokować interfejsu JavaFX
        new Thread(() -> {
            try {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    // Używamy callbacku, aby przekazać wiadomość do kontrolera FXML
                    onMessageReceived.accept(fromServer);
                }
            } catch (IOException e) {
                // Zwykle występuje, gdy zamykamy aplikację
                System.out.println("Połączenie z serwerem zostało zamknięte.");
            }
        }).start();
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}