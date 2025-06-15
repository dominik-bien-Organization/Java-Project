package com.example.clinicapp.client.service;

import com.example.clinicapp.model.Patient;
import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client-side service for patient-related operations.
 * This class uses network communication to interact with the server instead of directly accessing the database.
 */
public class PatientService {
    private final ClinicClient client;

    public PatientService(ClinicClient client) {
        this.client = client;
    }

    public Optional<Patient> login(String username, String password) {
        try {
            // Use the new PATIENT_LOGIN message type for authentication
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.PATIENT_LOGIN, username + ":" + password),
                    MessageType.PATIENT_LOGIN_SUCCESS);

            if (response == null) {
                return Optional.empty();
            }

            return Optional.of((Patient) response.getPayload());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public List<Patient> getAllPatients() {
        try {
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.GET_ALL_PATIENTS, null),
                    MessageType.ALL_PATIENTS_LIST);

            if (response == null) {
                return new ArrayList<>();
            }

            return (List<Patient>) response.getPayload();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean register(String email, String username, String password) throws Exception {
        try {
            String payload = email + ":" + username + ":" + password;
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.REGISTER_PATIENT, payload),
                    MessageType.PATIENT_REGISTERED);

            return response != null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new Exception("Failed to register patient: " + e.getMessage());
        }
    }
}
