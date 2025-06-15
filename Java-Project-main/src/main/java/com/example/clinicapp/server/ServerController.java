
package com.example.clinicapp.server;

import java.io.IOException;
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
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> logsListView;
    @FXML
    private ListView<String> clientsListView;
    private ObservableList<String> logs = FXCollections.observableArrayList();
    private ObservableList<String> clients = FXCollections.observableArrayList();
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean isRunning = true;
    private static Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap();
    private ObservableList<String> clientsObservableList = FXCollections.observableArrayList();



    @FXML
    public void initialize() {
        this.logsListView.setItems(this.logs);
        this.clientsListView.setItems(this.clients);
        clientsListView.setItems(clientsObservableList);
        this.startServer();
    }

    private void startServer() {
        this.serverThread = new Thread(this::runServer);
        this.serverThread.setDaemon(true);
        this.serverThread.start();
    }

    private void runServer() {
        try {
            this.serverSocket = new ServerSocket(12345);
            Platform.runLater(() -> {
                this.statusLabel.setText("Status: Uruchomiony na porcie 12345");
                this.logMessage("Serwer uruchomiony.");
            });

            while(this.isRunning) {
                Socket clientSocket = this.serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlers,
                        this::logClientActivity, this::updateClientsList);
                (new Thread(clientHandler)).start();
            }
        } catch (IOException e) {
            if (this.isRunning) {
                Platform.runLater(() -> this.logMessage("Błąd serwera: " + e.getMessage()));
            }
        }

    }

    private void logClientActivity(String message) {
        Platform.runLater(() -> {
            this.logMessage(message);
            this.updateClientsList(clientHandlers);
        });
    }

    private void logMessage(String message) {
        ObservableList var10000 = this.logs;
        String var10002 = String.valueOf(LocalTime.now().withNano(0));
        var10000.add(0, var10002 + ": " + message);
    }

    private void updateClientList() {
        List<String> fullNames = clientHandlers.values().stream()
                .map(ClientHandler::getFullName)
                .collect(Collectors.toList());
        this.clients.setAll(fullNames);
    }

    public void shutdownServer() {
        this.isRunning = false;

        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.logMessage("Serwer zatrzymany.");
    }

    public void updateClientsList(Map<String, ClientHandler> clients) {
        Platform.runLater(() -> {
            clientsObservableList.clear();
            clientsObservableList.addAll(clients.keySet());
        });
    }
}