package com.example.clinicapp.model;

import java.io.Serializable;
import java.sql.Date;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String email;
    protected String password;
    protected Date date;

    protected User(int id, String email, String password, Date date) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.date = date;
    }

    public boolean isValid() {
        return isValidEmail(email) && isValidPassword(password);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(".+@.+\\..+");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public java.sql.Date getDate() { return date; }

    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setDate(java.sql.Date date) { this.date = date; }
}
