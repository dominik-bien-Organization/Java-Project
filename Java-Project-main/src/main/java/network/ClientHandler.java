// package com.example.clinicapp.network;
package network;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Map<String, ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String clientUserId; // ID zalogowanego użytkownika (np. "doctor_123")

    public ClientHandler(Socket socket, Map<String, ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Odebrano od klienta (" + (clientUserId != null ? clientUserId : "N/A") + "): " + inputLine);
                handleMessage(inputLine);
            }
        } catch (IOException e) {
            System.out.println("Klient " + (clientUserId != null ? clientUserId : "") + " rozłączony.");
        } finally {
            // Usuń klienta z mapy po rozłączeniu
            if (clientUserId != null) {
                clients.remove(clientUserId);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String message) {
        String[] parts = message.split(":", 2); // Dzieli na komendę i resztę
        String command = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "LOGIN":
                // data format: <USER_TYPE>:<USER_ID>
                String[] loginData = data.split(":");
                String userType = loginData[0];
                this.clientUserId = userType + "_" + loginData[1]; // np. "PACJENT_kowalski"
                clients.put(this.clientUserId, this);
                System.out.println("Użytkownik " + this.clientUserId + " zalogowany.");
                break;

            case "BOOK_APPOINTMENT":
                // data format: <DOCTOR_ID>:<PATIENT_ID>:<DATE>:<TIME>
                String[] appointmentData = data.split(":");
                String doctorId = "LEKARZ_" + appointmentData[0];
                String patientId = "PACJENT_" + appointmentData[1];
                String date = appointmentData[2];
                String time = appointmentData[3];

                // Tutaj powinieneś zapisać wizytę do bazy danych
                // Database.saveAppointment(doctorId, patientId, date, time);
                System.out.println("Rezerwacja wizyty dla lekarza " + doctorId + " od pacjenta " + patientId);

                // Znajdź handlera dla lekarza i wyślij mu powiadomienie
                ClientHandler doctorHandler = clients.get(doctorId);
                if (doctorHandler != null) {
                    String notification = "NOTIFICATION:Nowa wizyta od pacjenta " + patientId.split("_")[1] + " na " + date + " o " + time;
                    doctorHandler.sendMessage(notification);
                } else {
                    System.out.println("Lekarz " + doctorId + " nie jest online.");
                    // Można dodać logikę do zapisywania powiadomień "offline" w bazie
                }

                // Wyślij potwierdzenie do pacjenta
                sendMessage("BOOKING_CONFIRMED:Wizyta u lekarza " + doctorId.split("_")[1] + " została potwierdzona.");
                break;

            default:
                System.out.println("Nieznana komenda: " + command);
                break;
        }
    }

    // Metoda do wysyłania wiadomości do klienta, którego obsługuje ten wątek
    public void sendMessage(String message) {
        out.println(message);
    }
}