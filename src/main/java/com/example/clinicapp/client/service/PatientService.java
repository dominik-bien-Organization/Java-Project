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
        // Login is still handled by the original login message
        // The server already handles authentication and returns a success/failure message
        // We don't need to change this method to use a new message type
        
        // For now, we'll continue to use the database directly for login
        // This will be replaced with server-side authentication in a future update
        return new com.example.clinicapp.service.PatientService().login(username, password);
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