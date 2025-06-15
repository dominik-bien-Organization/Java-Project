package com.example.clinicapp.model;

import com.example.clinicapp.interfaces.IDoctor;

import java.sql.Date;

public class Doctor extends User implements IDoctor {
    private String fullname;

    private Doctor(int id, String fullname, String email, String password, java.sql.Date date) {
        super(id, email, password, date);
        this.fullname = fullname;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && fullname != null && !fullname.isBlank();
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public static class Builder {
        private int _id = (int) (Math.random() * Integer.MAX_VALUE);
        private String _fullname, _email, _password;
        private Date _date;

        public Builder id(int id) {
            _id = id;
            return this;
        }

        public Builder fullname(String fullname) {
            _fullname = fullname;
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

        public Doctor build() {
            if (_fullname == null) {
                throw new NullPointerException("Username is null");
            }
            if (_email == null) {
                throw new NullPointerException("Email is null");
            }
            if (_password == null) {
                throw new NullPointerException("Password is null");
            }
            return new Doctor(_id, _fullname, _email, _password, _date);
        }
    }
}
