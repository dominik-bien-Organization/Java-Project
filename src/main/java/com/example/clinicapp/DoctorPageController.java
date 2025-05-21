package com.example.clinicapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DoctorPageController implements Initializable {

    @FXML
    private Button login_button;

    @FXML
    private CheckBox login_checkbox;

    @FXML
    private ComboBox<?> login_combobox;

    @FXML
    private TextField login_doctorID;

    @FXML
    private AnchorPane login_form;

    @FXML
    private PasswordField login_password;

    @FXML
    private TextField login_showPassword;

    @FXML
    private Hyperlink login_registerHere;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button register_button;

    @FXML
    private CheckBox register_checkbox;

    @FXML
    private TextField register_doctorID;

    @FXML
    private TextField register_email;

    @FXML
    private AnchorPane register_form;

    @FXML
    private TextField register_fullName;

    @FXML
    private Hyperlink register_loginHere;

    @FXML
    private PasswordField register_password;

    @FXML
    private TextField register_showPassword;

    @FXML
    void loginAccount(ActionEvent event) {

    }

    @FXML
    void loginShowPassword(ActionEvent event) {

    }

    @FXML
    void registerAccount(ActionEvent event) {

    }

    @FXML
    void registerShowPassword(ActionEvent event) {

    }



    @FXML
    public void switchForm(javafx.event.ActionEvent event) {
        if(event.getSource() == login_registerHere) {
            // register form visible
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
            // login form visible
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
