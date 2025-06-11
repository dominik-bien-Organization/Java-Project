package com.example.clinicapp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ServerController {
    private static final int SERVER_PORT = 12345;
    private static final String SERVER_IP = "192.168.1.111";

    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> logsListView;
    @FXML
    private ListView<String> clientsListView;

    private ObservableList<String> logs = FXCollections.observableArrayList();
    private ObservableList<String> clients = FXCollections.observableArrayList();
    private ObservableList<String> clientsObservableList = FXCollections.observableArrayList();

    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean isRunning = true;

    private static Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    @FXML
    public void initialize() {
        logsListView.setItems(logs);
        clientsListView.setItems(clientsObservableList);
        startServer();
    }

    private void startServer() {
        serverThread = new Thread(this::runServer);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void runServer() {
        try {
            InetAddress bindAddress = InetAddress.getByName(SERVER_IP); // <– konkretny adres IP
            serverSocket = new ServerSocket(SERVER_PORT, 50, bindAddress);

            Platform.runLater(() -> {
                statusLabel.setText("Status: Uruchomiony na " + SERVER_IP + ":" + SERVER_PORT);
                logMessage("Serwer uruchomiony.");
            });

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        clientHandlers,
                        this::logClientActivity,
                        this::updateClientsList
                );
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            if (isRunning) {
                Platform.runLater(() -> logMessage("Błąd serwera: " + e.getMessage()));
            }
        }
    }

    private void logClientActivity(String message) {
        Platform.runLater(() -> {
            logMessage(message);
            updateClientsList(clientHandlers);
        });
    }

    private void logMessage(String message) {
        String time = String.valueOf(LocalTime.now().withNano(0));
        logs.add(0, time + ": " + message);
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

    public void updateClientsList(Map<String, ClientHandler> clients) {
        Platform.runLater(() -> {
            clientsObservableList.clear();
            clientsObservableList.addAll(clients.keySet());
        });
    }
}
