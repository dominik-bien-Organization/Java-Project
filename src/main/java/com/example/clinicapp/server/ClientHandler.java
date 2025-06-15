package com.example.clinicapp.server;

import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.model.Patient;
import com.example.clinicapp.network.Appointment;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.network.Recipe;
import com.example.clinicapp.service.AppointmentService;
import com.example.clinicapp.service.DoctorService;
import com.example.clinicapp.service.PatientService;
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
import java.util.Optional;
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
    private final DoctorService doctorService = new DoctorService();
    private final PatientService patientService = new PatientService();
    private final RecipeService recipeService = new RecipeService();

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

                // Send login confirmation response
                sendMessage(new NetworkMessage(MessageType.LOGIN, "Login successful"));
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

                // Send logout confirmation response
                sendMessage(new NetworkMessage(MessageType.LOGOUT, "Logout successful"));

                try {
                    clientSocket.shutdownInput(); // <---- TO JEST KLUCZ
                    clientSocket.close();         // to wywoła wyjątek w run()
                } catch (IOException e) {
                    e.printStackTrace();
                }
                running = false;
                return;


            // Doctor related messages
            case DOCTOR_LOGIN:
                String[] doctorLoginData = ((String)message.getPayload()).split(":");
                String doctorEmail = doctorLoginData[0];
                String doctorPassword = doctorLoginData[1];

                try {
                    Doctor authenticatedDoctor = doctorService.login(doctorEmail, doctorPassword);
                    if (authenticatedDoctor != null) {
                        // Store client information
                        this.clientKey = doctorEmail;
                        this.fullName = "Dr " + authenticatedDoctor.getFullname() + " (id: " + authenticatedDoctor.getId() + ")";
                        this.doctorId = authenticatedDoctor.getId();

                        this.clients.put(this.clientKey, this);
                        this.logger.accept("Zalogowano lekarza: " + this.clientKey + " (" + this.fullName + ")");
                        this.clientsListUpdater.accept(this.clients);

                        // Send success response with the doctor object
                        sendMessage(new NetworkMessage(MessageType.DOCTOR_LOGIN_SUCCESS, authenticatedDoctor));
                    } else {
                        sendMessage(new NetworkMessage(MessageType.DOCTOR_LOGIN_FAILED, "Invalid email or password"));
                        logger.accept("Logowanie lekarza nieudane - nieprawidłowy email lub hasło: " + doctorEmail);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendMessage(new NetworkMessage(MessageType.DOCTOR_LOGIN_FAILED, e.getMessage()));
                    logger.accept("Błąd podczas logowania lekarza: " + e.getMessage());
                }
                break;

            case REGISTER_DOCTOR:
                Doctor doctor = (Doctor) message.getPayload();
                try {
                    if (doctorService.isEmailExists(doctor.getEmail())) {
                        sendMessage(new NetworkMessage(MessageType.DOCTOR_REGISTER_FAILED, "Email already exists"));
                        logger.accept("Rejestracja lekarza nieudana - email już istnieje: " + doctor.getEmail());
                    } else {
                        doctorService.register(doctor);
                        sendMessage(new NetworkMessage(MessageType.DOCTOR_REGISTERED, null));
                        logger.accept("Zarejestrowano lekarza: " + doctor.getFullname());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendMessage(new NetworkMessage(MessageType.DOCTOR_REGISTER_FAILED, e.getMessage()));
                    logger.accept("Błąd podczas rejestracji lekarza: " + e.getMessage());
                }
                break;

            case CHECK_DOCTOR_EMAIL_EXISTS:
                String emailToCheck = (String) message.getPayload();
                try {
                    boolean exists = doctorService.isEmailExists(emailToCheck);
                    sendMessage(new NetworkMessage(MessageType.DOCTOR_EMAIL_EXISTS_RESULT, exists));
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendMessage(new NetworkMessage(MessageType.DOCTOR_EMAIL_EXISTS_RESULT, false));
                    logger.accept("Błąd podczas sprawdzania emaila lekarza: " + e.getMessage());
                }
                break;

            case GET_ALL_DOCTORS:
                List<Doctor> doctors = doctorService.getAllDoctors();
                sendMessage(new NetworkMessage(MessageType.ALL_DOCTORS_LIST, doctors));
                break;

            // Patient related messages
            case PATIENT_LOGIN:
                String[] patientLoginData = ((String)message.getPayload()).split(":");
                String patientUsername = patientLoginData[0];
                String patientPassword = patientLoginData[1];

                try {
                    Optional<Patient> patientOpt = patientService.login(patientUsername, patientPassword);
                    if (patientOpt.isPresent()) {
                        Patient authenticatedPatient = patientOpt.get();

                        // Store client information
                        this.clientKey = patientUsername;
                        this.fullName = authenticatedPatient.getUsername();

                        this.clients.put(this.clientKey, this);
                        this.logger.accept("Zalogowano pacjenta: " + this.clientKey);
                        this.clientsListUpdater.accept(this.clients);

                        // Send success response with the patient object
                        sendMessage(new NetworkMessage(MessageType.PATIENT_LOGIN_SUCCESS, authenticatedPatient));
                    } else {
                        sendMessage(new NetworkMessage(MessageType.PATIENT_LOGIN_FAILED, "Invalid username or password"));
                        logger.accept("Logowanie pacjenta nieudane - nieprawidłowa nazwa użytkownika lub hasło: " + patientUsername);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(new NetworkMessage(MessageType.PATIENT_LOGIN_FAILED, e.getMessage()));
                    logger.accept("Błąd podczas logowania pacjenta: " + e.getMessage());
                }
                break;

            case REGISTER_PATIENT:
                String[] patientData = ((String) message.getPayload()).split(":");
                String patientEmail = patientData[0];
                String patientUsernameReg = patientData[1];
                String patientPasswordReg = patientData[2];

                try {
                    boolean registered = patientService.register(patientEmail, patientUsernameReg, patientPasswordReg);
                    if (registered) {
                        sendMessage(new NetworkMessage(MessageType.PATIENT_REGISTERED, null));
                        logger.accept("Zarejestrowano pacjenta: " + patientUsernameReg);
                    } else {
                        sendMessage(new NetworkMessage(MessageType.PATIENT_REGISTER_FAILED, "Username already exists"));
                        logger.accept("Rejestracja pacjenta nieudana - nazwa użytkownika już istnieje: " + patientUsernameReg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(new NetworkMessage(MessageType.PATIENT_REGISTER_FAILED, e.getMessage()));
                    logger.accept("Błąd podczas rejestracji pacjenta: " + e.getMessage());
                }
                break;

            case GET_ALL_PATIENTS:
                List<Patient> patients = patientService.getAllPatients();
                sendMessage(new NetworkMessage(MessageType.ALL_PATIENTS_LIST, patients));
                break;

            // Recipe related messages
            case GET_RECIPES_FOR_PATIENT:
                int patientId = (int) message.getPayload();
                List<Recipe> recipes = recipeService.getRecipesByPatientId(patientId);
                sendMessage(new NetworkMessage(MessageType.PATIENT_RECIPES_LIST, recipes));
                break;

            // Statistics related messages
            case GET_PATIENTS_LAST_7_DAYS:
                Map<String, Integer> patientsData = doctorService.getPatientsLast7Days();
                sendMessage(new NetworkMessage(MessageType.PATIENTS_LAST_7_DAYS_DATA, patientsData));
                break;

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
