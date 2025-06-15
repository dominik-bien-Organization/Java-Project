package com.example.clinicapp.client.service;

import com.example.clinicapp.network.ClinicClient;
import com.example.clinicapp.network.MessageType;
import com.example.clinicapp.network.NetworkMessage;
import com.example.clinicapp.network.Recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side service for recipe-related operations.
 * This class uses network communication to interact with the server instead of directly accessing the database.
 */
public class RecipeService {
    private final ClinicClient client;

    public RecipeService(ClinicClient client) {
        this.client = client;
    }

    public boolean saveRecipe(Recipe recipe) {
        try {
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.SAVE_RECIPE, recipe),
                    MessageType.RECIPE_SAVED);
            
            return response != null;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Recipe> getRecipesByPatientId(int patientId) {
        try {
            NetworkMessage response = client.sendMessageAndWaitForResponse(
                    new NetworkMessage(MessageType.GET_RECIPES_FOR_PATIENT, patientId),
                    MessageType.PATIENT_RECIPES_LIST);
            
            if (response == null) {
                return new ArrayList<>();
            }
            
            return (List<Recipe>) response.getPayload();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}