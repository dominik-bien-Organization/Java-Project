// package com.example.clinicapp.network;
package com.example.clinicapp.network;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 2L;

    private final int doctorId;
    private final int patientId;
    private final String patientUsername;
    private final LocalDate date;
    private final LocalTime time;

    public Appointment(int doctorId, int patientId, String patientUsername, LocalDate date, LocalTime time) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.patientUsername = patientUsername;
        this.date = date;
        this.time = time;
    }

    // Gettery
    public int getDoctorId() { return doctorId; }
    public int getPatientId() { return patientId; }
    public String getPatientUsername() { return patientUsername; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }

    @Override
    public String toString() {
        return "Wizyta u dr " + doctorId + " dla pacjenta " + patientUsername + " dnia " + date + " o " + time;
    }
}