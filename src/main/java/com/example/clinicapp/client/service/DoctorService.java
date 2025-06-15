package com.example.clinicapp.client.service;

import com.example.clinicapp.model.Doctor;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client-side service for doctor-related operations.
 * This class uses network communication to interact with the server instead of directly accessing the database.
 */
public class DoctorService {
    private final ClinicClient client;

    public DoctorService(ClinicClient client) {
        this.client = client;
    }

    public Doctor login(String email, String password) throws SQLException, IOException, InterruptedException {
        // Use the new DOCTOR_LOGIN message type for authentication
        NetworkMessage response = client.sendMessageAndWaitForResponse(
                new NetworkMessage(MessageType.DOCTOR_LOGIN, email + ":" + password),
                MessageType.DOCTOR_LOGIN_SUCCESS);

        if (response == null) {
            throw new IOException("No response from server");
        }

        return (Doctor) response.getPayload();
    }

    public boolean isEmailExists(String email) throws SQLException, IOException, InterruptedException {
        NetworkMessage response = client.sendMessageAndWaitForResponse(
                new NetworkMessage(MessageType.CHECK_DOCTOR_EMAIL_EXISTS, email),
                MessageType.DOCTOR_EMAIL_EXISTS_RESULT);

        if (response == null) {
            throw new IOException("No response from server");
        }

        return (boolean) response.getPayload();
    }

    public void register(Doctor doctor) throws SQLException, IOException, InterruptedException {
        NetworkMessage response = client.sendMessageAndWaitForResponse(
                new NetworkMessage(MessageType.REGISTER_DOCTOR, doctor),
                MessageType.DOCTOR_REGISTERED);

        if (response == null) {
            throw new IOException("No response from server");
        }

        // If we get here, the registration was successful
    }

    public List<Doctor> getAllDoctors() {
        try {
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.GET_ALL_DOCTORS, null),
                    MessageType.ALL_DOCTORS_LIST);

            if (response == null) {
                return new ArrayList<>();
            }

            return (List<Doctor>) response.getPayload();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, Integer> getPatientsLast7Days() {
        try {
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.GET_PATIENTS_LAST_7_DAYS, null),
                    MessageType.PATIENTS_LAST_7_DAYS_DATA);

            if (response == null) {
                return Map.of();
            }

            return (Map<String, Integer>) response.getPayload();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public int getDoctorIdByFullName(String fullname) {
        // This method is not used in the client code, so we don't need to implement it
        // If needed, we would add a new message type for this operation
        return -1;
    }

    public boolean updatePassword(int doctorId, String newPassword) throws SQLException {
        // This method is not used in the client code, so we don't need to implement it
        // If needed, we would add a new message type for this operation
        return false;
    }

    public boolean deleteDoctor(int doctorId) throws SQLException {
        // This method is not used in the client code, so we don't need to implement it
        // If needed, we would add a new message type for this operation
        return false;
    }
}
