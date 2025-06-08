// package com.example.clinicapp.server;
package com.example.clinicapp.server;

import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Map<String, ClientHandler> clients;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientKey; // Klucz w mapie (np. "LEKARZ_1", "PACJENT_5")
    private final Consumer<String> logger; // Callback do logowania na GUI serwera

    public ClientHandler(Socket socket, Map<String, ClientHandler> clients, Consumer<String> logger) {
        this.clientSocket = socket;
        this.clients = clients;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            NetworkMessage messageFromClient;
            while ((messageFromClient = (NetworkMessage) in.readObject()) != null) {
                handleMessage(messageFromClient);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.accept("Klient " + (clientKey != null ? clientKey : "") + " rozłączony.");
        } finally {
            if (clientKey != null) {
                clients.remove(clientKey);
                logger.accept("Usunięto z listy: " + clientKey + ". Aktywni: " + clients.size());
            }
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case LOGIN:
                String[] loginData = ((String) message.getPayload()).split(":");
                this.clientKey = loginData[0] + "_" + loginData[1];
                clients.put(this.clientKey, this);
                logger.accept("Zalogowano: " + this.clientKey + ". Aktywni: " + clients.size());
                break;

            case BOOK_APPOINTMENT:
                Appointment appointment = (Appointment) message.getPayload();
                logger.accept("Odebrano prośbę o rezerwację: " + appointment);

                // TODO: Zapisz wizytę w bazie danych serwera.

                // Powiadom lekarza
                String doctorKey = "LEKARZ_" + appointment.getDoctorId();
                ClientHandler doctorHandler = clients.get(doctorKey);
                if (doctorHandler != null) {
                    NetworkMessage notification = new NetworkMessage(MessageType.APPOINTMENT_NOTIFICATION, appointment);
                    doctorHandler.sendMessage(notification);
                    logger.accept("Wysłano powiadomienie do: " + doctorKey);
                } else {
                    logger.accept("Lekarz " + doctorKey + " jest offline. Powiadomienie zostanie dostarczone po zalogowaniu (funkcjonalność do dodania).");
                }

                // Potwierdź pacjentowi
                NetworkMessage confirmation = new NetworkMessage(MessageType.BOOKING_CONFIRMED, "Twoja wizyta została pomyślnie zarezerwowana!");
                sendMessage(confirmation);
                break;

            default:
                logger.accept("Odebrano nieznany typ wiadomości: " + message.getType());
                break;
        }
    }

    public void sendMessage(NetworkMessage message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            logger.accept("Błąd podczas wysyłania wiadomości do " + clientKey + ": " + e.getMessage());
        }
    }
}