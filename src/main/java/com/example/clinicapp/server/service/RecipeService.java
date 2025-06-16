package com.example.clinicapp.server.service;

import com.example.clinicapp.server.database.DatabaseConnector;
import com.example.clinicapp.common.network.Recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecipeService {

    public boolean saveRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipe (patient_id, doctor_id, description, issue_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recipe.getPatientId());
            ps.setInt(2, recipe.getDoctorId());
            ps.setString(3, recipe.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(recipe.getIssueDate()));

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Recipe> getRecipesByPatientId(int patientId) {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT r.id, r.patient_id, r.doctor_id, r.description, r.issue_date, d.fullname as doctor_name " +
                "FROM recipe r " +
                "JOIN doctor d ON r.doctor_id = d.doctorID " +
                "WHERE r.patient_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = new Recipe(
                            rs.getInt("id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getString("description"),
                            rs.getDate("issue_date").toLocalDate()
                    );
                    recipe.setDoctorName(rs.getString("doctor_name")); // dodaj pole doctorName w Recipe!
                    recipes.add(recipe);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipes;
    }
}
