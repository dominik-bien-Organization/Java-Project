package com.example.clinicapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class DoctorPageController implements Initializable {

    @FXML
    private Button login_button;

    @FXML
    private CheckBox login_checkbox;

    @FXML
    private AnchorPane login_form;

    @FXML
    private PasswordField login_password;

    @FXML
    private Hyperlink login_registerHere;

    @FXML
    private TextField login_email;

    @FXML
    private TextField login_showPassword;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button register_button;

    @FXML
    private CheckBox register_checkbox;

    @FXML
    private TextField register_showPassword;
    @FXML
    private TextField register_email;

    @FXML
    private AnchorPane register_form;

    @FXML
    private Hyperlink register_loginHere;

    @FXML
    private PasswordField register_password;




    @FXML
    private TextField register_fullName;



    //DataBase tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private AlertMessage alert = new AlertMessage();


    public void loginAccount() {
        String username = login_email.getText();
        String password;

        if (login_checkbox.isSelected()) {

            password = login_showPassword.getText();
        } else {

            password = login_password.getText();
        }

        if (username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
        } else {
            String sql = "SELECT * FROM doctor WHERE email = ? AND password = ?";

            connect = Database.connectDb();

            try {
                prepare = connect.prepareStatement(sql);
                prepare.setString(1, username);
                prepare.setString(2, password);
                result = prepare.executeQuery();

                if (result.next()) {
                    alert.successMessage("Logowanie wykonano pomyślnie!");

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ClinicSystemPage.fxml"));
                    Parent root = loader.load();


                    Stage stage = (Stage) login_button.getScene().getWindow();


                    stage.setScene(new Scene(root));
                    stage.setTitle("Clinic System");
                    stage.show();


                } else {
                    alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void registerAccount() {

        connect = Database.connectDb();

        String fullname = register_fullName.getText();
        String email = register_email.getText();
        String password;

        if (register_showPassword.isVisible()) {
            password = register_showPassword.getText();
        } else {
            password = register_password.getText();
        }

        // Walidacja
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie wymagane pola");
            return;
        }

        try {
            connect = Database.connectDb(); // <--- koniecznie

            // Sprawdź, czy lekarz o takim email już istnieje
            String checkEmail = "SELECT * FROM doctor WHERE email = ?";
            prepare = connect.prepareStatement(checkEmail);
            prepare.setString(1, email);
            result = prepare.executeQuery();

            if (result.next()) {
                alert.errorMessage("Lekarz z takim emailem już istnieje w bazie danych.");
            } else if (password.length() < 8) {
                alert.errorMessage("Hasło musi mieć przynajmniej 8 znaków");
            } else {
                String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";
                prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());

                prepare.setString(1, fullname);
                prepare.setString(2, email);
                prepare.setString(3, password);
                prepare.setDate(4, sqlDate);

                prepare.executeUpdate();
                alert.successMessage("Rejestracja wykonana pomyślnie!");
                registerClear();

                login_form.setVisible(true);
                register_form.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Błąd rejestracji: " + e.getMessage());
        }
    }
    public void loginShowPassword() {

        if (login_checkbox.isSelected()) {
            login_showPassword.setText(login_password.getText());
            login_showPassword.setVisible(true);
            login_password.setVisible(false);
        } else {
            login_password.setText(login_showPassword.getText());
            login_showPassword.setVisible(false);
            login_password.setVisible(true);
        }
    }

    public void registerShowPassword() {

        if(register_checkbox.isSelected()) {
            register_showPassword.setText(register_password.getText());
            register_showPassword.setVisible(true);
            register_password.setVisible(false);
        }
        else {
            register_password.setText(register_showPassword.getText());
            register_showPassword.setVisible(false);
            register_password.setVisible(true);
        }
    }

    public void registerClear() {
        register_email.clear();
        register_fullName.clear();
        register_password.clear();
        register_showPassword.clear();
    }

    @FXML
    public void switchForm(ActionEvent event) {
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

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 
    }
}
