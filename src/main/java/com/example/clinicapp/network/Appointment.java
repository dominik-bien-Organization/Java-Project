package com.example.clinicapp.network;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 2L;
    private int patientId;
    private String patientName; //
    private int doctorId;
    private LocalDate date;
    private LocalTime time;

    public Appointment(int doctorId, int patientId, String patientUsername, LocalDate date, LocalTime time) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.patientName = patientUsername;
        this.date = date;
        this.time = time;
    }
    public Appointment() {
        // konstruktor bezargumentowy, może być pusty
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String toString() {
        int var10000 = this.doctorId;
        return "Wizyta u dr " + var10000 + " dla pacjenta " + this.patientName + " dnia " + String.valueOf(this.date) + " o " + String.valueOf(this.time);
    }
}