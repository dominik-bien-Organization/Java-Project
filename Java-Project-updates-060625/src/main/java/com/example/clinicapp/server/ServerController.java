// package com.example.clinicapp.server;
package com.example.clinicapp.server;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController {

    @FXML private Label statusLabel;
    @FXML private ListView<String> logsListView;
    @FXML private ListView<String> clientsListView;

    private ObservableList<String> logs = FXCollections.observableArrayList();
    private ObservableList<String> clients = FXCollections.observableArrayList();

    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean isRunning = true;

    // Mapa klientów musi być dostępna dla kontrolera, aby mógł on aktualizować listę
    // FIX: Teraz odwołuje się do poprawnej klasy ClientHandler z pakietu server
    private static Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();


    @FXML
    public void initialize() {
        logsListView.setItems(logs);
        clientsListView.setItems(clients);
        startServer();
    }

    private void startServer() {
        serverThread = new Thread(this::runServer);
        serverThread.setDaemon(true); // Wątek zakończy się, gdy zamkniemy aplikację
        serverThread.start();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(12345);
            Platform.runLater(() -> {
                statusLabel.setText("Status: Uruchomiony na porcie 12345");
                logMessage("Serwer uruchomiony.");
            });

            while (isRunning) {
                Socket clientSocket = serverSocket.accept(); // Czeka na połączenie
                Platform.runLater(() -> logMessage("Nowy klient połączony: " + clientSocket.getInetAddress()));

                // Tworzymy ClientHandler z callbackiem do logowania
                // FIX: To wywołanie jest teraz poprawne, bo używa ClientHandler z tego samego pakietu
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlers, this::logClientActivity);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                Platform.runLater(() -> logMessage("Błąd serwera: " + e.getMessage()));
            }
        }
    }

    // Ta metoda jest wywoływana z ClientHandler
    private void logClientActivity(String message) {
        Platform.runLater(() -> {
            logMessage(message);
            updateClientList();
        });
    }

    private void logMessage(String message) {
        // Dodajemy na początek listy, aby najnowsze logi były na górze
        logs.add(0, java.time.LocalTime.now().withNano(0) + ": " + message);
    }

    private void updateClientList() {
        clients.setAll(clientHandlers.keySet());
    }

    public void shutdownServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logMessage("Serwer zatrzymany.");
    }
}