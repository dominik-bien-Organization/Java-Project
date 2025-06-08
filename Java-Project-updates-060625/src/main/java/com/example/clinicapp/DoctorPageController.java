/*package com.example.clinicapp;

import javafx.application.Platform;
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
import java.util.Date;
import java.util.ResourceBundle;

public class DoctorPageController implements Initializable {

    // Wszystkie Twoje pola @FXML dla formularza logowania/rejestracji
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_email;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_fullName;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private AlertMessage alert = new AlertMessage();


    public void loginAccount() {
        String email = login_email.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            return;
        }

        // FIX: Poprawione zapytanie SQL. Zamiast 'id' używamy 'doctorID',
        // które jest rzeczywistą nazwą kolumny w tabeli 'doctor'.
        String sql = "SELECT doctorID, fullname FROM doctor WHERE email = ? AND password = ?";
        connect = Database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, email);
            prepare.setString(2, password);
            result = prepare.executeQuery();

            if (result.next()) {
                // FIX: Pobieramy 'doctorID' zamiast 'id'.
                int doctorId = result.getInt("doctorID");
                String doctorFullName = result.getString("fullname");
                UserData userData = new UserData(doctorId, doctorFullName, "Lekarz");

                alert.successMessage("Logowanie wykonano pomyślnie!");

                Stage currentStage = (Stage) login_button.getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
                Parent root = loader.load();

                DoctorDashboardController dashboardController = loader.getController();
                dashboardController.initData(userData);

                Stage stage = new Stage();
                stage.setTitle("Panel Lekarza - " + doctorFullName);
                stage.setScene(new Scene(root));
                stage.show();

                stage.setOnCloseRequest(event -> {
                    dashboardController.closeClientConnection();
                    // Usunięto Platform.exit(), aby zamknięcie jednego klienta nie zamykało całej aplikacji
                    // (np. drugiego klienta lub serwera, jeśli działają w tym samym procesie).
                });

            } else {
                alert.errorMessage("Nieprawidłowy email lub hasło");
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Wystąpił błąd: " + e.getMessage());
        }
    }

    public void registerAccount() {
        // ... (kod rejestracji jest generalnie OK)
        // Można dodać walidację formatu email.
        String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";
        try {
            connect = Database.connectDb();
            // ... (logika sprawdzania czy email istnieje) ...
            if (result.next()) {
                // ...
            } else {
                prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(1, register_fullName.getText());
                prepare.setString(2, register_email.getText());
                prepare.setString(3, register_password.getText());
                // FIX: Użycie setDate dla kolumny typu DATE jest bezpieczniejsze.
                prepare.setDate(4, sqlDate);
                prepare.executeUpdate();

                alert.successMessage("Rejestracja wykonana pomyślnie!");
                // ...
            }
        } catch(Exception e) {
            e.printStackTrace();
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
        } else {
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
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}*/
package com.example.clinicapp;

import javafx.application.Platform;
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
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DoctorPageController implements Initializable {

    // Wszystkie Twoje pola @FXML
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_email;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_fullName;

    private AlertMessage alert = new AlertMessage();

    // === NOWA WERSJA METODY LOGINACCOUNT ===
    public void loginAccount() {
        String email = login_email.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            return;
        }

        // 1. Zablokuj przycisk
        login_button.setDisable(true);
        login_button.setText("Logowanie...");

        // 2. Utwórz zadanie Callable
        Callable<UserData> loginTask = () -> {
            String sql = "SELECT doctorID, fullname FROM doctor WHERE email = ? AND password = ?";
            try (Connection connect = Database.connectDb();
                 PreparedStatement prepare = connect.prepareStatement(sql)) {

                prepare.setString(1, email);
                prepare.setString(2, password);
                try (ResultSet result = prepare.executeQuery()) {
                    if (result.next()) {
                        int doctorId = result.getInt("doctorID");
                        String doctorFullName = result.getString("fullname");
                        return new UserData(doctorId, doctorFullName, "Lekarz");
                    } else {
                        return null; // Błąd logowania
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Błąd bazy danych
            }
        };

        // 3. Opakuj w FutureTask
        FutureTask<UserData> futureTask = new FutureTask<>(loginTask);

        // 4. Uruchom w nowym wątku
        Thread thread = new Thread(() -> {
            try {
                futureTask.run();
                UserData userData = futureTask.get();

                // 5. Zaktualizuj UI w wątku JavaFX
                Platform.runLater(() -> processLoginResult(userData));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    alert.errorMessage("Wystąpił nieoczekiwany błąd podczas logowania.");
                    resetLoginButton();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Metoda pomocnicza do obsługi wyniku logowania
    private void processLoginResult(UserData userData) {
        if (userData != null) {
            alert.successMessage("Logowanie wykonano pomyślnie!");
            openDoctorDashboard(userData);
        } else {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do otwierania panelu lekarza
    private void openDoctorDashboard(UserData userData) {
        try {
            Stage currentStage = (Stage) login_button.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
            Parent root = loader.load();

            DoctorDashboardController dashboardController = loader.getController();
            dashboardController.initData(userData);

            Stage stage = new Stage();
            stage.setTitle("Panel Lekarza - " + userData.getUsername());
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnCloseRequest(event -> {
                dashboardController.closeClientConnection();
            });
        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Błąd podczas ładowania panelu lekarza.");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do resetowania przycisku
    private void resetLoginButton() {
        login_button.setDisable(false);
        login_button.setText("Zaloguj");
    }


    public void registerAccount() {
        // ... (kod rejestracji jest generalnie OK, zostaje bez zmian)
        String fullName = register_fullName.getText();
        String email = register_email.getText();
        String password = register_password.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę wypełnić wszystkie pola.");
            return;
        }

        String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";
        try (Connection connect = Database.connectDb()) {
            String checkEmail = "SELECT email FROM doctor WHERE email = ?";
            PreparedStatement checkStmt = connect.prepareStatement(checkEmail);
            checkStmt.setString(1, email);
            ResultSet result = checkStmt.executeQuery();

            if (result.next()) {
                alert.errorMessage("Email " + email + " jest już zajęty.");
            } else {
                PreparedStatement prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(1, fullName);
                prepare.setString(2, email);
                prepare.setString(3, password);
                prepare.setDate(4, sqlDate);
                prepare.executeUpdate();

                alert.successMessage("Rejestracja wykonana pomyślnie!");
                registerClear();
                login_form.setVisible(true);
                register_form.setVisible(false);
            }
        } catch(Exception e) {
            e.printStackTrace();
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
        } else {
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
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}/*package com.example.clinicapp;

import javafx.application.Platform;
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
import java.util.Date;
import java.util.ResourceBundle;

public class DoctorPageController implements Initializable {

    // Wszystkie Twoje pola @FXML dla formularza logowania/rejestracji
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_email;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_fullName;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private AlertMessage alert = new AlertMessage();


    public void loginAccount() {
        String email = login_email.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            return;
        }

        // FIX: Poprawione zapytanie SQL. Zamiast 'id' używamy 'doctorID',
        // które jest rzeczywistą nazwą kolumny w tabeli 'doctor'.
        String sql = "SELECT doctorID, fullname FROM doctor WHERE email = ? AND password = ?";
        connect = Database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, email);
            prepare.setString(2, password);
            result = prepare.executeQuery();

            if (result.next()) {
                // FIX: Pobieramy 'doctorID' zamiast 'id'.
                int doctorId = result.getInt("doctorID");
                String doctorFullName = result.getString("fullname");
                UserData userData = new UserData(doctorId, doctorFullName, "Lekarz");

                alert.successMessage("Logowanie wykonano pomyślnie!");

                Stage currentStage = (Stage) login_button.getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
                Parent root = loader.load();

                DoctorDashboardController dashboardController = loader.getController();
                dashboardController.initData(userData);

                Stage stage = new Stage();
                stage.setTitle("Panel Lekarza - " + doctorFullName);
                stage.setScene(new Scene(root));
                stage.show();

                stage.setOnCloseRequest(event -> {
                    dashboardController.closeClientConnection();
                    // Usunięto Platform.exit(), aby zamknięcie jednego klienta nie zamykało całej aplikacji
                    // (np. drugiego klienta lub serwera, jeśli działają w tym samym procesie).
                });

            } else {
                alert.errorMessage("Nieprawidłowy email lub hasło");
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Wystąpił błąd: " + e.getMessage());
        }
    }

    public void registerAccount() {
        // ... (kod rejestracji jest generalnie OK)
        // Można dodać walidację formatu email.
        String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";
        try {
            connect = Database.connectDb();
            // ... (logika sprawdzania czy email istnieje) ...
            if (result.next()) {
                // ...
            } else {
                prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(1, register_fullName.getText());
                prepare.setString(2, register_email.getText());
                prepare.setString(3, register_password.getText());
                // FIX: Użycie setDate dla kolumny typu DATE jest bezpieczniejsze.
                prepare.setDate(4, sqlDate);
                prepare.executeUpdate();

                alert.successMessage("Rejestracja wykonana pomyślnie!");
                // ...
            }
        } catch(Exception e) {
            e.printStackTrace();
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
        } else {
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
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}*/
package com.example.clinicapp;

import javafx.application.Platform;
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
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DoctorPageController implements Initializable {

    // Wszystkie Twoje pola @FXML
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_email;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_fullName;

    private AlertMessage alert = new AlertMessage();

    // === NOWA WERSJA METODY LOGINACCOUNT ===
    public void loginAccount() {
        String email = login_email.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            return;
        }

        // 1. Zablokuj przycisk
        login_button.setDisable(true);
        login_button.setText("Logowanie...");

        // 2. Utwórz zadanie Callable
        Callable<UserData> loginTask = () -> {
            String sql = "SELECT doctorID, fullname FROM doctor WHERE email = ? AND password = ?";
            try (Connection connect = Database.connectDb();
                 PreparedStatement prepare = connect.prepareStatement(sql)) {

                prepare.setString(1, email);
                prepare.setString(2, password);
                try (ResultSet result = prepare.executeQuery()) {
                    if (result.next()) {
                        int doctorId = result.getInt("doctorID");
                        String doctorFullName = result.getString("fullname");
                        return new UserData(doctorId, doctorFullName, "Lekarz");
                    } else {
                        return null; // Błąd logowania
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Błąd bazy danych
            }
        };

        // 3. Opakuj w FutureTask
        FutureTask<UserData> futureTask = new FutureTask<>(loginTask);

        // 4. Uruchom w nowym wątku
        Thread thread = new Thread(() -> {
            try {
                futureTask.run();
                UserData userData = futureTask.get();

                // 5. Zaktualizuj UI w wątku JavaFX
                Platform.runLater(() -> processLoginResult(userData));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    alert.errorMessage("Wystąpił nieoczekiwany błąd podczas logowania.");
                    resetLoginButton();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Metoda pomocnicza do obsługi wyniku logowania
    private void processLoginResult(UserData userData) {
        if (userData != null) {
            alert.successMessage("Logowanie wykonano pomyślnie!");
            openDoctorDashboard(userData);
        } else {
            alert.errorMessage("Nieprawidłowy email lub hasło");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do otwierania panelu lekarza
    private void openDoctorDashboard(UserData userData) {
        try {
            Stage currentStage = (Stage) login_button.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
            Parent root = loader.load();

            DoctorDashboardController dashboardController = loader.getController();
            dashboardController.initData(userData);

            Stage stage = new Stage();
            stage.setTitle("Panel Lekarza - " + userData.getUsername());
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnCloseRequest(event -> {
                dashboardController.closeClientConnection();
            });
        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Błąd podczas ładowania panelu lekarza.");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do resetowania przycisku
    private void resetLoginButton() {
        login_button.setDisable(false);
        login_button.setText("Zaloguj");
    }


    public void registerAccount() {
        // ... (kod rejestracji jest generalnie OK, zostaje bez zmian)
        String fullName = register_fullName.getText();
        String email = register_email.getText();
        String password = register_password.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę wypełnić wszystkie pola.");
            return;
        }

        String insertData = "INSERT INTO doctor (fullname, email, password, date) VALUES (?, ?, ?, ?)";
        try (Connection connect = Database.connectDb()) {
            String checkEmail = "SELECT email FROM doctor WHERE email = ?";
            PreparedStatement checkStmt = connect.prepareStatement(checkEmail);
            checkStmt.setString(1, email);
            ResultSet result = checkStmt.executeQuery();

            if (result.next()) {
                alert.errorMessage("Email " + email + " jest już zajęty.");
            } else {
                PreparedStatement prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());
                prepare.setString(1, fullName);
                prepare.setString(2, email);
                prepare.setString(3, password);
                prepare.setDate(4, sqlDate);
                prepare.executeUpdate();

                alert.successMessage("Rejestracja wykonana pomyślnie!");
                registerClear();
                login_form.setVisible(true);
                register_form.setVisible(false);
            }
        } catch(Exception e) {
            e.printStackTrace();
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
        } else {
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
            login_form.setVisible(false);
            register_form.setVisible(true);
        } else if(event.getSource() == register_loginHere) {
            login_form.setVisible(true);
            register_form.setVisible(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
