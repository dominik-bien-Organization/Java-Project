package com.example.clinicapp.model;

import com.example.clinicapp.interfaces.IPatient;

import java.sql.Date;

public class Patient extends User implements IPatient {

    private String username;

    private Patient(int id, String username, String email, String password, java.sql.Date date) {
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

    public static class Builder {
        private int _id = (int) (Math.random() * Integer.MAX_VALUE);
        private String _username, _email, _password;
        private Date _date;

        public Builder id(int id) {
            _id = id;
            return this;
        }

        public Builder username(String username) {
            _username = username;
            return this;
        }

        public Builder email(String email) {
            _email = email;
            return this;
        }

        public Builder password(String password) {
            _password = password;
            return this;
        }

        public Builder date(Date date) {
            _date = date;
            return this;
        }

        public Patient build() {
            if (_username == null) {
                throw new NullPointerException("Username is null");
            }
            if (_email == null) {
                throw new NullPointerException("Email is null");
            }
            if (_password == null) {
                throw new NullPointerException("Password is null");
            }
            if (_date == null) {
                throw new NullPointerException("Date is null");
            }
            return new Patient(_id, _username, _email, _password, _date);
        }
    }
}
