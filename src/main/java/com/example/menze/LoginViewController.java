package com.example.menze;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Korisnik;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginViewController {
    private static Korisnik korisnik;

    @FXML
    private TextField user;
    @FXML
    private PasswordField pass;

    private static final Logger logger = LoggerFactory.getLogger(LoginViewController.class);
    private static ResourceBundle bundle = MainController.getBundle();

    @FXML
    public void initialize(){
        logger.info("Example log from {}", LoginViewController.class.getSimpleName());
    }

    public void login(){
        String username = user.getText(), password = pass.getText();
        if(username.equals("") || password.equals("")) {
            if (username.equals(""))
                user.setStyle("-fx-border-color: red");
            else
                user.setStyle(null);
            if (password.equals(""))
                pass.setStyle("-fx-border-color: red");
            else
                pass.setStyle(null);
        }

        else
        {
            try{
                korisnik = Database.getKorisnik(username,password);
                logger.info("Uspješno dohvaćen korisnik iz baze podataka.");
            }catch (SQLException | IOException | NoSuchAlgorithmException e){
                e.printStackTrace();
                logger.error("Greška pri učitavanju korisnika iz baze podataka.");
            }
            if(korisnik == null)
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pogreška");
                alert.setHeaderText(null);
                alert.setContentText("Ne postoji korisnik s upisanim podacima!");
                alert.showAndWait();
                pass.clear();
                user.clear();
                MainController.ponovnaPrijava(MainController.getLoginStage());

            }else {
                MainController.getLoginStage().close();
                if(korisnik.getPristup().equals("menza"))
                    MainController.prijavljenoMenza(korisnik);
                else
                    MainController.prijavljenoStudent(korisnik);
            }
        }
    }




    public void showRegisterView(){
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("register-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 269, 394);
            logger.info("Uspješno postavljena scena za prikaz registracijskog ekrana.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz registracijskog ekrana.");
        }

        MainController.getLoginStage().setScene(scene);
        MainController.getLoginStage().show();
    }
}
