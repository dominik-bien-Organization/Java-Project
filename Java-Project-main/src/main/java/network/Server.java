// package com.example.clinicapp.network;
package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 12345;
    // Mapa do przechowywania handlerów dla zalogowanych klientów (bezpieczna dla wątków)
    // Klucz: ID użytkownika (np. "doctor_1", "patient_5"), Wartość: obiekt ClientHandler
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Serwer kliniki uruchomiony...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Akceptuj nowe połączenie - to jest operacja blokująca
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy klient połączony: " + clientSocket);

                // Dla każdego klienta utwórz nowy wątek do obsługi
                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }
}