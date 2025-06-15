package com.example.clinicapp.server;

import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.network.Recipe;
import com.example.clinicapp.service.AppointmentService;
import com.example.clinicapp.service.RecipeService;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;



public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Map<String, ClientHandler> clients;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientKey;
    private final Consumer<String> logger;
    private final Consumer<Map<String, ClientHandler>> clientsListUpdater;
    private String fullName;
    private volatile boolean running = true;
    private int doctorId = -1;
    private final AppointmentService appointmentService = new AppointmentService();

    public ClientHandler(Socket socket, Map<String, ClientHandler> clients,
                         Consumer<String> logger, Consumer<Map<String, ClientHandler>> clientsListUpdater) {
        this.clientSocket = socket;
        this.clients = clients;
        this.logger = logger;
        this.clientsListUpdater = clientsListUpdater;
    }

    public void run() {
        try {
            this.out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.in = new ObjectInputStream(this.clientSocket.getInputStream());

            while (running) {
                try {
                    NetworkMessage messageFromClient = (NetworkMessage) this.in.readObject();
                    handleMessage(messageFromClient);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if (clientKey != null)
                logger.accept("Klient " + clientKey + " rozłączony.");
            else
                e.printStackTrace();  // Tu dodaj printStackTrace, by zobaczyć inne błędy
        }finally {
            if (clientKey != null) {
                clients.remove(clientKey);
                logger.accept("Usunięto z listy: " + clientKey + ". Aktywni: " + clients.size());
                clientsListUpdater.accept(clients);
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
                String[] loginData = ((String)message.getPayload()).split(":");
                this.clientKey = loginData[0];  // email
                this.fullName = loginData.length > 1 ? loginData[1] : loginData[0];

                // Przypisz doctorId jeśli to lekarz
                if (this.fullName.startsWith("Dr ")) {
                    this.doctorId = extractDoctorIdFromFullName(this.fullName);
                }

                this.clients.put(this.clientKey, this);
                this.logger.accept("Zalogowano: " + this.clientKey + " (" + this.fullName + ")");
                this.clientsListUpdater.accept(this.clients);
                break;

            case BOOK_APPOINTMENT:
                Appointment appointment = (Appointment) message.getPayload();
                logger.accept("Otrzymano prośbę o rezerwację wizyty: " + appointment);

                boolean success = appointmentService.saveAppointment(appointment);

                try {
                    if (success) {
                        out.writeObject(new NetworkMessage(MessageType.BOOKING_CONFIRMED, null));
                        logger.accept("Wizyta zapisana pomyślnie: " + appointment);
                    } else {
                        out.writeObject(new NetworkMessage(MessageType.BOOKING_CANCELLED, null));
                        logger.accept("Nie udało się zapisać wizyty: " + appointment);
                    }
                    out.flush();
                } catch (IOException e) {
                    logger.accept("Błąd podczas wysyłania potwierdzenia rezerwacji: " + e.getMessage());
                }
                break;
            case GET_APPOINTMENTS_FOR_DOCTOR:
                int doctorId = (int) message.getPayload();
                try {
                    List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(doctorId);
                    sendMessage(new NetworkMessage(MessageType.APPOINTMENT_LIST, appointments));
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Możesz tu wysłać wiadomość o błędzie lub pustą listę
                    sendMessage(new NetworkMessage(MessageType.APPOINTMENT_LIST, new ArrayList<>()));
                }
                break;
            case SAVE_RECIPE:
                Recipe recipe = (Recipe) message.getPayload();
                RecipeService recipeService = new RecipeService();
                boolean saved = recipeService.saveRecipe(recipe);


                    if (saved) {
                        sendMessage(new NetworkMessage(MessageType.RECIPE_SAVED, null));
                        logger.accept("Recepta zapisana: " + recipe);
                    } else {
                        sendMessage(new NetworkMessage(MessageType.RECIPE_SAVE_FAILED, null));
                        logger.accept("Nie udało się zapisać recepty: " + recipe);
                    }

                break;
            case LOGOUT:
                String logoutEmail = (String) message.getPayload();
                logger.accept("Wylogowano: " + logoutEmail + ". Aktywni: " + (clients.size() - 1));
                clients.remove(logoutEmail);
                clientsListUpdater.accept(clients);

                try {
                    clientSocket.shutdownInput(); // <---- TO JEST KLUCZ
                    clientSocket.close();         // to wywoła wyjątek w run()
                } catch (IOException e) {
                    e.printStackTrace();
                }
                running = false;
                return;


            default:
                this.logger.accept("Odebrano nieznany typ wiadomości: " + String.valueOf(message.getType()));


        }



    }

    public String getFullName() {
        return fullName;
    }
    private int extractDoctorIdFromFullName(String fullName) {
        // Przykładowo: "Dr Jan Kowalski (id: 3)"
        if (fullName.contains("(id:")) {
            try {
                String idPart = fullName.substring(fullName.indexOf("(id:") + 4, fullName.indexOf(")"));
                return Integer.parseInt(idPart.trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public void sendMessage(NetworkMessage message) {
        try {
            if (this.out != null) {
                this.out.writeObject(message);
                this.out.flush();
            }
        } catch (IOException e) {
            String var10001 = this.clientKey;
            this.logger.accept("Błąd podczas wysyłania wiadomości do " + var10001 + ": " + e.getMessage());
        }

    }


}