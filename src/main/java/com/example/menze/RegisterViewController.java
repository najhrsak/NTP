package com.example.menze;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RegisterViewController {
    @FXML
    private TextField jmbag;
    @FXML
    private TextField ime;
    @FXML
    private TextField prezime;
    @FXML
    private TextField eAdresa;
    @FXML
    private TextField fakultet;
    @FXML
    private TextField user;
    @FXML
    private PasswordField pass;
    @FXML
    private PasswordField passconf;
    @FXML
    private Label poruka;
    @FXML
    private Label putanja;

    private static final Logger logger = LoggerFactory.getLogger(RegisterViewController.class);
    private static ResourceBundle bundle = MainController.getBundle();


    @FXML
    private void initialize(){
        logger.info("Example log from {}", RegisterViewController.class.getSimpleName());
        FileChooser.ExtensionFilter fcef = new FileChooser.ExtensionFilter("Image Files", "*.jpg");
        fc.getExtensionFilters().add(fcef);
    }

    public void register() {
        String uneseniJMBAG = jmbag.getText(), unesenoIme = ime.getText(), unesenoPrezime = prezime.getText();
        String unesenaEAdresa = eAdresa.getText(), uneseniFakultet = fakultet.getText();
        String username = user.getText(), password = pass.getText(), confPassword = passconf.getText();

        if(username.equals("") || password.equals("") || uneseniJMBAG.equals("") || unesenoIme.equals("")
        || unesenoPrezime.equals("") || uneseniFakultet.equals("") || unesenaEAdresa.equals("") || confPassword.equals("")
        || !password.equals(confPassword) || putanja.getText().equals("")) {
            if (username.equals(""))
                user.setStyle("-fx-border-color: red");
            else
                user.setStyle(null);
            if (password.equals(""))
                pass.setStyle("-fx-border-color: red");
            else
                pass.setStyle(null);
            if (confPassword.equals(""))
                passconf.setStyle("-fx-border-color: red");
            else
                passconf.setStyle(null);
            if (!confPassword.equals(password))
            {
                poruka.setText("Lozinke nisu iste!");
                passconf.setStyle("-fx-border-color: red");
                pass.setStyle("-fx-border-color: red");
            }
            else
            {
                poruka.setText("");
                passconf.setStyle(null);
                pass.setStyle(null);
            }
            if (uneseniJMBAG.equals(""))
                jmbag.setStyle("-fx-border-color: red");
            else
                jmbag.setStyle(null);
            if (unesenoIme.equals(""))
                ime.setStyle("-fx-border-color: red");
            else
                ime.setStyle(null);
            if (unesenoPrezime.equals(""))
                prezime.setStyle("-fx-border-color: red");
            else
                prezime.setStyle(null);
            if (unesenaEAdresa.equals(""))
                eAdresa.setStyle("-fx-border-color: red");
            else
                eAdresa.setStyle(null);
            if (uneseniFakultet.equals(""))
                fakultet.setStyle("-fx-border-color: red");
            else
                fakultet.setStyle(null);
            if(putanja.getText().equals(""))
                putanja.setStyle("-fx-border-color: red");
            else
                putanja.setStyle(null);
        }
        else{
            MainController.getLoginStage().close();
            Student student = new Student(0, username, "student", uneseniJMBAG, unesenoIme, unesenoPrezime,
                    unesenaEAdresa, uneseniFakultet, in);
            try{
                Database.newStudent(student, password);
                logger.info("Uspješno upisan novi student u bazu podataka.");
            }catch (SQLException | IOException e){
                e.printStackTrace();
                logger.error("Greška pri upisu novog studenta u bazu podataka.");
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Uspješna prijava");
            alert.setHeaderText("Uspješno ste se prijavili!");
            alert.setContentText("Sada se možete prijaviti.");
            alert.showAndWait();
        }

    }

    public void showLoginView() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 214, 210);
            logger.info("Uspješno postavljena scena za prikaz ekrana za prijavu.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za prijavu.");
        }
        MainController.getLoginStage().setScene(scene);
        MainController.getLoginStage().show();
    }

    private FileChooser fc = new FileChooser();
    private InputStream in;

    public void odaberiSliku() {
        File chosen = fc.showOpenDialog(MainController.getLoginStage());
        try{
            putanja.setText(chosen.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            in = new FileInputStream(chosen);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
