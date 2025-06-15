package com.example.clinicapp.network;

import java.io.Serializable;
import java.time.LocalDate;

public class Recipe implements Serializable {
    private static final long serialVersionUID = 3L;
    private String patientName;    // username pacjenta
    private String description;    // opis recepty
    private LocalDate issueDate;   // data wystawienia
    private int doctorId;          // id lekarza
    private int patientId;         // id pacjenta
    private String doctorName;
    private int id;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // konstruktor, gettery, settery

    public Recipe() {

    }

    public Recipe(int id, int patientId, int doctorId, String description, LocalDate issueDate) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.description = description;
        this.issueDate = issueDate;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
}
