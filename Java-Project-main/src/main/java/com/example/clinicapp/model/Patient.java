package com.example.clinicapp.model;

import com.example.clinicapp.interfaces.IPatient;

public class Patient extends User implements IPatient {
    private String username;

    public Patient(int id, String username, String email, String password, java.sql.Date date) {
        super(id, email, password, date);
        this.username = username;
    }

    @Override
    public String toString() {
        return username + " (" + getId() + ")";
    }

    @Override
    public boolean isValid() {
        return super.isValid() && username != null && !username.isBlank();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
