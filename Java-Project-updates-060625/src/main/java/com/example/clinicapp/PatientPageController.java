/*package com.example.clinicapp;

import com.example.clinicapp.network.ClinicClient;
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

public class PatientPageController implements Initializable {

    // Tutaj zostają wszystkie twoje pola @FXML dla formularza logowania i rejestracji
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_username;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_username;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private AlertMessage alert = new AlertMessage();

    // METODA LOGINACCOUNT JEST NAJWAŻNIEJSZA - ZOBACZ ZMIANY
    public void loginAccount() {
        String username = login_username.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
            return;
        }

        // Zmienione zapytanie SQL, aby pobrać również 'id'
        String sql = "SELECT id, username FROM patient WHERE username = ? AND password = ?";
        connect = Database.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, username);
            prepare.setString(2, password);
            result = prepare.executeQuery();

            if (result.next()) {
                // POBIERAMY DANE UŻYTKOWNIKA Z BAZY
                int userId = result.getInt("id");
                String dbUsername = result.getString("username");
                UserData userData = new UserData(userId, dbUsername, "Pacjent");

                alert.successMessage("Logowanie wykonano pomyślnie!");

                // ZAMYKAMY OKNO LOGOWANIA
                Stage currentStage = (Stage) login_button.getScene().getWindow();
                currentStage.close();

                // OTWIERAMY NOWE OKNO - PANEL PACJENTA I PRZEKAZUJEMY DANE
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PatientDashboard.fxml")); // <-- NOWY PLIK FXML
                Parent root = loader.load();

                // Pobieramy kontroler nowego okna
                PatientDashboardController dashboardController = loader.getController();
                // Przekazujemy do niego dane zalogowanego użytkownika
                dashboardController.initData(userData);

                Stage stage = new Stage();
                stage.setTitle("Panel Pacjenta - " + dbUsername);
                stage.setScene(new Scene(root));
                stage.show();

                stage.setOnCloseRequest(event -> {
                    dashboardController.closeClientConnection();
                });

            } else {
                alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert.errorMessage("Wystąpił błąd: " + e.getMessage());
        }
    }


    // Reszta twoich metod (registerAccount, showPassword, etc.) zostaje bez zmian.
    // Poniżej wklejam je dla kompletności.

    public void registerAccount() {
        String email = register_email.getText();
        String username = register_username.getText();
        String password = register_showPassword.isVisible() ? register_showPassword.getText() : register_password.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie wymagane pola");
            return;
        }
        try {
            String checkUsername = "SELECT * FROM patient WHERE username = ?";
            connect = Database.connectDb();
            prepare = connect.prepareStatement(checkUsername);
            prepare.setString(1, username);
            result = prepare.executeQuery();

            if (result.next()) {
                alert.errorMessage("Użytkownik " + username + " już istnieje w bazie danych.");
            } else if (password.length() < 8) {
                alert.errorMessage("Hasło nieprawidłowe. Musi mieć przynajmniej 8 znaków");
            } else {
                String insertData = "INSERT INTO patient (email, username, password, date) VALUES (?, ?, ?, ?)";
                prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());

                prepare.setString(1, email);
                prepare.setString(2, username);
                prepare.setString(3, password);
                // FIX: Zmieniono setString(4, sqlDate.toString()) na setDate(4, sqlDate).
                // Jest to poprawny sposób wstawiania daty do kolumny SQL typu DATE.
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
        } else {
            register_password.setText(register_showPassword.getText());
            register_showPassword.setVisible(false);
            register_password.setVisible(true);
        }
    }

    public void registerClear() {
        register_email.clear();
        register_username.clear();
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
    public void initialize(URL url, ResourceBundle rb) {}

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

public class PatientPageController implements Initializable {

    // Tutaj zostają wszystkie twoje pola @FXML dla formularza logowania i rejestracji
    @FXML private Button login_button;
    @FXML private CheckBox login_checkbox;
    @FXML private AnchorPane login_form;
    @FXML private PasswordField login_password;
    @FXML private Hyperlink login_registerHere;
    @FXML private TextField login_username;
    @FXML private TextField login_showPassword;
    @FXML private AnchorPane main_form;
    @FXML private Button register_button;
    @FXML private CheckBox register_checkbox;
    @FXML private TextField register_showPassword;
    @FXML private TextField register_email;
    @FXML private AnchorPane register_form;
    @FXML private Hyperlink register_loginHere;
    @FXML private PasswordField register_password;
    @FXML private TextField register_username;

    // Pola do obsługi bazy danych i alertów
    private AlertMessage alert = new AlertMessage();

    // === NOWA WERSJA METODY LOGINACCOUNT ===
    public void loginAccount() {
        String username = login_username.getText();
        String password = login_checkbox.isSelected() ? login_showPassword.getText() : login_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
            return;
        }

        // 1. Zablokuj przycisk, aby zapobiec wielokrotnemu klikaniu
        login_button.setDisable(true);
        login_button.setText("Logowanie...");

        // 2. Utwórz zadanie Callable, które wykona zapytanie do bazy i zwróci UserData lub null
        Callable<UserData> loginTask = () -> {
            String sql = "SELECT id, username FROM patient WHERE username = ? AND password = ?";
            // Używamy try-with-resources, aby zapewnić zamknięcie połączenia
            try (Connection connect = Database.connectDb();
                 PreparedStatement prepare = connect.prepareStatement(sql)) {

                prepare.setString(1, username);
                prepare.setString(2, password);
                try (ResultSet result = prepare.executeQuery()) {
                    if (result.next()) {
                        // Sukces: pobieramy dane i tworzymy obiekt UserData
                        int userId = result.getInt("id");
                        String dbUsername = result.getString("username");
                        return new UserData(userId, dbUsername, "Pacjent");
                    } else {
                        // Błąd logowania (złe dane): zwracamy null
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Błąd (np. bazy danych): również zwracamy null
                return null;
            }
        };

        // 3. Opakuj Callable w FutureTask
        FutureTask<UserData> futureTask = new FutureTask<>(loginTask);

        // 4. Uruchom FutureTask w nowym wątku
        Thread thread = new Thread(() -> {
            try {
                // Uruchamiamy zadanie w tym wątku
                futureTask.run();
                // Czekamy na wynik i go pobieramy. Ta linia blokuje TEN wątek, a nie wątek UI.
                UserData userData = futureTask.get();

                // 5. Po otrzymaniu wyniku, wróć do wątku JavaFX, aby zaktualizować UI
                Platform.runLater(() -> {
                    processLoginResult(userData);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    alert.errorMessage("Wystąpił nieoczekiwany błąd podczas logowania.");
                    resetLoginButton();
                });
            }
        });
        thread.setDaemon(true); // Wątek zakończy się, gdy zamkniemy aplikację
        thread.start();
    }

    // Metoda pomocnicza do obsługi wyniku logowania w wątku UI
    private void processLoginResult(UserData userData) {
        if (userData != null) {
            // Sukces logowania
            alert.successMessage("Logowanie wykonano pomyślnie!");
            openPatientDashboard(userData);
        } else {
            // Błąd logowania
            alert.errorMessage("Nieprawidłowa nazwa użytkownika lub hasło");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do otwierania nowego okna
    private void openPatientDashboard(UserData userData) {
        try {
            // ZAMYKAMY OKNO LOGOWANIA
            Stage currentStage = (Stage) login_button.getScene().getWindow();
            currentStage.close();

            // OTWIERAMY NOWE OKNO - PANEL PACJENTA I PRZEKAZUJEMY DANE
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PatientDashboard.fxml"));
            Parent root = loader.load();

            // Pobieramy kontroler nowego okna
            PatientDashboardController dashboardController = loader.getController();
            // Przekazujemy do niego dane zalogowanego użytkownika
            dashboardController.initData(userData);

            Stage stage = new Stage();
            stage.setTitle("Panel Pacjenta - " + userData.getUsername());
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnCloseRequest(event -> {
                dashboardController.closeClientConnection();
            });
        } catch (IOException e) {
            e.printStackTrace();
            alert.errorMessage("Błąd podczas ładowania panelu pacjenta.");
            resetLoginButton();
        }
    }

    // Metoda pomocnicza do resetowania przycisku logowania
    private void resetLoginButton() {
        login_button.setDisable(false);
        login_button.setText("Zaloguj");
    }

    // Reszta twoich metod (registerAccount, showPassword, etc.) zostaje bez zmian.
    // Poniżej wklejam je dla kompletności.

    public void registerAccount() {
        String email = register_email.getText();
        String username = register_username.getText();
        String password = register_showPassword.isVisible() ? register_showPassword.getText() : register_password.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            alert.errorMessage("Proszę uzupełnić wszystkie wymagane pola");
            return;
        }
        try(Connection connect = Database.connectDb()) {
            String checkUsername = "SELECT * FROM patient WHERE username = ?";
            PreparedStatement prepare = connect.prepareStatement(checkUsername);
            prepare.setString(1, username);
            ResultSet result = prepare.executeQuery();

            if (result.next()) {
                alert.errorMessage("Użytkownik " + username + " już istnieje w bazie danych.");
            } else if (password.length() < 8) {
                alert.errorMessage("Hasło nieprawidłowe. Musi mieć przynajmniej 8 znaków");
            } else {
                String insertData = "INSERT INTO patient (email, username, password, date) VALUES (?, ?, ?, ?)";
                prepare = connect.prepareStatement(insertData);
                java.sql.Date sqlDate = new java.sql.Date(new java.util.Date().getTime());

                prepare.setString(1, email);
                prepare.setString(2, username);
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
        } else {
            register_password.setText(register_showPassword.getText());
            register_showPassword.setVisible(false);
            register_password.setVisible(true);
        }
    }

    public void registerClear() {
        register_email.clear();
        register_username.clear();
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
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("LoginChoice.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
