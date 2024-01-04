package com.example.menze;

import database.Database;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.ClientSocket;
import model.Korisnik;
import model.Menza;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainController {
    private static Korisnik k;
    private static boolean prijavljeno = false;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static boolean otvorioSveMenze;
    private static Locale locale;

    @FXML
    private Button sveMenzeButton;
    @FXML
    private Button prijavaButton;
    @FXML
    private Label prikaz;
    @FXML
    private Label prijava;
    @FXML
    private Label pozz;
    @FXML
    private Label profil;
    @FXML
    private Button odjava;
    @FXML
    private Button prikazProfila;

    private static ResourceBundle bundle = ResourceBundle.getBundle("strings");;

    @FXML
    public void initialize(){
        prikaz.setText(bundle.getString("prikaz"));
        sveMenzeButton.setText(bundle.getString("prikazM"));
        if(!prijavljeno){
            prijava.setText(bundle.getString("prijava"));
            prijavaButton.setText(bundle.getString("prijavaG"));
        }
        else{
            profil.setText(bundle.getString("profile"));
            prikazProfila.setText(bundle.getString("prikaz_profil"));
            odjava.setText(bundle.getString("odjava"));
            if(k.getPristup().equals("student")) {
                ClientSocket clientSocket = new ClientSocket();
                pozz.setText(clientSocket.run(k.getUsername()));
            }
        }

        logger.info("Example log from {}", MainController.class.getSimpleName());


    }

    public void changeToHr() {
        locale = new Locale("hr");
        bundle = ResourceBundle.getBundle("strings", locale);
        prikaz.setText(bundle.getString("prikaz"));
        prijava.setText(bundle.getString("prijava"));
        sveMenzeButton.setText(bundle.getString("prikazM"));
        prijavaButton.setText(bundle.getString("prijavaG"));
    }

    public void changeToEn(ActionEvent actionEvent) {
        locale = new Locale("en");
        bundle = ResourceBundle.getBundle("strings", locale);
        prikaz.setText(bundle.getString("prikaz"));
        prijava.setText(bundle.getString("prijava"));
        sveMenzeButton.setText(bundle.getString("prikazM"));
        prijavaButton.setText(bundle.getString("prijavaG"));
    }

    public static ResourceBundle getBundle(){
        return bundle;
    }

    public static void setOtvorioSveMenze(boolean stanje){
        otvorioSveMenze = stanje;
    }
    public static boolean getOtvorioSveMenze(){
        return otvorioSveMenze;
    }
    public void sveMenze(){
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("sve-menze-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz svih menzi.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz svih menzi.");
        }
        SveMenzeViewController controller = fxmlLoader.getController();
        controller.setPreScene(sveMenzeButton.getScene());
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();
    }

    private static boolean daNe= false;
    public static void setDaNe(boolean daNe1){
        daNe = daNe1;
    }

    private static Stage loginStage;
    public void prijava() throws IOException{
        if(new File("dat/last_session.bin").exists()) {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("popup.fxml"));
            fxmlLoader.setResources(bundle);
            Scene scene = new Scene(fxmlLoader.load(), 218, 86);
            stage.setTitle("DANE");
            stage.setScene(scene);
            stage.showAndWait();
            if (daNe) {
                FileInputStream fis = new FileInputStream("dat/last_session.bin");
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    Korisnik korisnik = (Korisnik) ois.readObject();
                    if(korisnik.getPristup().equals("menza")){
                        prijavljenoMenza(korisnik);
                    }
                    else if(korisnik.getPristup().equals("student")){
                        prijavljenoStudent(korisnik);
                    }
                    logger.info("Korisnik uspješno pročitan.");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    logger.error("Greška pri čitanju korisnika iz datoteke.");
                }
                ois.close();
            } else {
                displayLoginView();
            }

        }else{
            displayLoginView();
        }
    }

    void displayLoginView(){
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 214, 210);
            logger.info("Postavljena scena za prikaz ekrana za prijavu.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za prijavu.");
        }
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
        loginStage = stage;
    }

    public static Stage getLoginStage(){return loginStage;}

    public static void ponovnaPrijava(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 214, 210);
            logger.info("Postavljena scena za prikaz ekrana za ponovnu prijavu.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za ponovnu prijavu.");
        }
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void prijavljenoMenza(Korisnik korisnik){
        k = korisnik;
        prijavljeno = true;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("logged-in-view-menza.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz ekrana kada je menza prijavljena.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana kada je prijavljena menza.");
        }
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();


    }
    public static void prijavljenoStudent(Korisnik korisnik){
        k = korisnik;
        prijavljeno = true;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("logged-in-view-student.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz ekrana kada je student prijavljen.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana kada je prijavljen student.");
        }

        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();

    }

    public static Korisnik getK(){
        return k;
    }
    public static void resetPrijava(){
        k=null;
        prijavljeno = false;
    }

    public void showStudentView() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("student-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz korisničkog sučelja za studenta.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz korisničkog sučelja za studenta.");
        }
        StudentViewController controller = fxmlLoader.getController();
        controller.setPreScene(sveMenzeButton.getScene());
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();
    }

    public void showMenzaView() {
        setOtvorioSveMenze(false);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("menza-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz korisničkog sučelja za menzu.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz korisničkog sučelja za menzu.");
        }
        MenzaViewController controller = fxmlLoader.getController();
        controller.setPreScene(sveMenzeButton.getScene());
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();

    }



    private static Stage menzaStage;

    public static void showMenza(){
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("menza-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz odabrane menze.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz odabrane menze.");
        }
        stage.setTitle("Menza");
        stage.setScene(scene);
        menzaStage = stage;
        stage.show();
    }

    public static Stage getMenzaStage(){return menzaStage;}

    public static boolean getPrijavljeno(){
        return prijavljeno;
    }

    public void logout(){
        k = null;
        prijavljeno = false;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("first-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz početnog ekrana.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz početnog ekrana.");
        }
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();
    }


    public void izdavanjeRacuna() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("izdavanje-racuna-view.fxml"));
        Scene scene = null;
        try{
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz korisničkog sučelja za studenta.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz korisničkog sučelja za studenta.");
        }
        IzdavanjeRacunaViewController controller = fxmlLoader.getController();
        controller.setPreScene(sveMenzeButton.getScene());
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();
    }


}