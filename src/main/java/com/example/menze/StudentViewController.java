package com.example.menze;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Korisnik;
import model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;


import static org.apache.commons.io.FileUtils.copyInputStreamToFile;


public class StudentViewController {
    @FXML
    private Label jmbag;
    @FXML
    private TextField fakultet;
    @FXML
    private Label ime;
    @FXML
    private TextField eAdresa;
    @FXML
    private Label username;
    @FXML
    private ImageView slika;
    @FXML
    private Button editButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button promjena;


    Student student;
    private InputStream in;

    private Scene preScene;
    private static final Logger logger = LoggerFactory.getLogger(StudentViewController.class);

    public void setPreScene(Scene preScene){
        this.preScene = preScene;
    }

    public void returnButton(){
        Stage stage = MainApplication.getStage();
        stage.setScene(preScene);
        stage.show();
    }

    @FXML
    public void initialize() throws FileNotFoundException {
        logger.info("Example log from {}", StudentViewController.class.getSimpleName());

        Korisnik korisnik = MainController.getK();
        try{
            student = Database.getStudentFromKorisnik(korisnik);
            logger.info("Uspješno dohvaćen korisnik iz baze podataka.");
        }catch (SQLException | IOException e)
        {
            e.printStackTrace();
            logger.error("Greška pri dohvaćanju korisnika iz baze podataka.");
        }
        jmbag.setText(student.getJmbag());
        fakultet.setText(student.getFakultet());
        ime.setText(student.getIme() + " " + student.getPrezime());
        eAdresa.setText(student.geteAdresa());
        username.setText(student.getUsername());
        in = student.getSlika();

        Image img = new Image(in);
        slika.setImage(img);

        FileChooser.ExtensionFilter fcef = new FileChooser.ExtensionFilter("Image Files", "*.jpg");
        fc.getExtensionFilters().add(fcef);
    }

    private FileChooser fc = new FileChooser();
    private File odabranaSlika = new File("dat/slika");;
    public void promjenaSlike() {
        odabranaSlika = fc.showOpenDialog(MainController.getMenzaStage());
        try{
            in = new FileInputStream(odabranaSlika);
            Image img = new Image(in);
            slika.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void editProfile() {
        editButton.setVisible(false);
        fakultet.setEditable(true);
        eAdresa.setEditable(true);
        promjena.setVisible(true);
        saveButton.setVisible(true);
    }

    public void saveProfile(){
        try{
            Database.updateStudent(jmbag.getText(), eAdresa.getText(), fakultet.getText(), new FileInputStream(odabranaSlika));
            System.out.println(in);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        editButton.setVisible(true);
        fakultet.setEditable(false);
        eAdresa.setEditable(false);
        promjena.setVisible(false);
        saveButton.setVisible(false);
        System.out.println(in);
    }

    public void deleteProfile() {
        try{
            Database.deleteStudent(student.getUsername());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        File file = new File("dat/last_session.bin");
        file.delete();
        MainController.resetPrijava();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("first-view.fxml"));
        Scene scene = null;
        try{
            fxmlLoader.setResources(MainController.getBundle());
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Postavljena scena za prikaz početnog ekrana.");
        }catch (IOException e){
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz početnog ekrana.");
        }
        MainApplication.getStage().setScene(scene);
        MainApplication.getStage().show();
    }
}
