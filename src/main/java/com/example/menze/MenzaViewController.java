package com.example.menze;

import DRETVE.DretvaPosjecenosti;
import DRETVE.DretvaPrekida;
import database.Database;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Korisnik;
import model.Menza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class MenzaViewController{
    @FXML
    private Button editRVButton;
    @FXML
    private Button editIButton;
    @FXML
    private TextArea info;
    @FXML
    private TextArea radnoVrijeme;
    @FXML
    private Button saveInfo;
    @FXML
    private Button saveRV;
    @FXML
    private Button saveDM;
    @FXML
    private Button editDMButton;
    @FXML
    private TextArea dnevniMeni;
    @FXML
    private Label adresa;
    @FXML
    private Label naziv;
    @FXML
    private Button ret;
    @FXML
    private Label postotakPosjecenosti;
    @FXML
    private Button jelovnik;

    private static Scene preScene;
    private Menza menza;
    private Korisnik korisnik = MainController.getK();
    private static Stage meniStage, insertMeniStage;
    private static Long posjecenost = ThreadLocalRandom.current().nextLong(0, 12)*100/12;
    static Timeline prikazPosjecenosti;
    private static final Logger logger = LoggerFactory.getLogger(MeniViewController.class);
    private static ResourceBundle bundle = MainController.getBundle();


    public void setPreScene(Scene preScene){
        this.preScene = preScene;
    }

    public static Scene getPreScene(){
        return preScene;
    }

    public void returnButton() {
        Stage stage = MainApplication.getStage();
        stage.setScene(preScene);
        stage.show();
        prikazPosjecenosti.stop();
    }

    @FXML
    public void initialize(){
        logger.info("Example log from {}", MenzaViewController.class.getSimpleName());
        jelovnik.setVisible(true);
        //grafika();

        if(SveMenzeViewController.getPrijavljenoMenza() && MainController.getOtvorioSveMenze())
            nisamPrijavljenKaoMenza();
        else {
            if (MainController.getPrijavljeno()) {
                if (korisnik.getPristup().equals("menza")) {
                    try {
                        menza = Database.getMenzaFromKorisnik(korisnik);
                        logger.info("Uspješno dohvaćena menza iz baze podataka.");
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                        logger.error("Greška pri dohvaćanju menze iz baze podataka.");
                    }

                    editRVButton.setVisible(true);
                    editDMButton.setVisible(true);
                    editIButton.setVisible(true);
                } else {
                    nisamPrijavljenKaoMenza();
                }
            } else {
                nisamPrijavljenKaoMenza();
            }
        }
        info.setText(menza.getInfo());
        radnoVrijeme.setText(menza.getRadnoVrijeme());
        dnevniMeni.setText(menza.getJelovnik());
        adresa.setText(menza.getAdresa() + ", " + menza.getGrad());
        naziv.setText(menza.getNaziv());


        prikazPosjecenosti = new Timeline( new KeyFrame(Duration.seconds(4),
                event -> {
                    Platform.runLater(new DretvaPosjecenosti());
                    postotakPosjecenosti.setText("Trenutna posjećenost menze: " + posjecenost.toString() + "%");
                }));
        prikazPosjecenosti.setCycleCount(Timeline.INDEFINITE);
        prikazPosjecenosti.play();
        //runDretva();
    }


    private void runDretva(){
        Platform.runLater(new DretvaPrekida());
    }
    void grafika(){
        editRVButton.setGraphic(new ImageView("dat/edit_button.png"));
        editRVButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        editIButton.setGraphic(new ImageView("dat/edit_button.png"));
        editDMButton.setGraphic(new ImageView("dat/edit_button.png"));
        String image = Menza.class.getResource("dat/edit_button.png.jpg").toExternalForm();
        editRVButton.setStyle("-fx-background-image: url('"+ image +"')");
        //editIButton.setStyle("-fx-background-image: url('dat/edit_button.png')");
        editDMButton.setStyle("-fx-background-image: 'dat/edit_button.png'");
    }

    public static Timeline getPrikazPosjecenosti(){
        return prikazPosjecenosti;
    }

    public void nisamPrijavljenKaoMenza() {
        editRVButton.setVisible(false);
        editDMButton.setVisible(false);
        editIButton.setVisible(false);
        ret.setVisible(false);
        jelovnik.setVisible(false);
        menza = SveMenzeViewController.getMenza();

        Platform.runLater(new DretvaPrekida());
    }

    public void editRadnoVrijeme() {
        radnoVrijeme.setEditable(true);
        saveRV.setVisible(true);
        editRVButton.setVisible(false);
        Client client = ClientBuilder.newClient();
        URI uri = URI.create("http://localhost:8081/rest/radno-vrijeme");
        Response response = client.target(uri).request().get();
        String rv = response.readEntity(String.class);
        radnoVrijeme.setText(rv);
    }

    public void editInfo() {
        info.setEditable(true);
        saveInfo.setVisible(true);
        editIButton.setVisible(false);
    }

    public void editDnevniMenu(){
        dnevniMeni.setEditable(true);
        saveDM.setVisible(true);
        editDMButton.setVisible(false);
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("insert-meni-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Uspješno postavljena scena za prikaz ekrana za unos novog jela.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za unos novog jela.");
        }
        stage.setTitle("Unos dnevnog menija");
        stage.setScene(scene);
        stage.show();
        insertMeniStage = stage;
    }

    public static Stage getInsertMeniStage(){
        return insertMeniStage;
    }

    public void saveInfoChanges() {
        info.setEditable(false);
        saveInfo.setVisible(false);
        editIButton.setVisible(true);
        String newInfo = info.getText();
        try{
            Database.updateTextField(newInfo, "info", korisnik);
            logger.info("Uspješno promijenjen info.");
        }catch (SQLException | IOException e){
            e.printStackTrace();
            logger.error("Greška pri promijeni infoa.");
        }
    }

    public void saveDMChanges() {
        dnevniMeni.setEditable(false);
        saveDM.setVisible(false);
        editDMButton.setVisible(true);
        String newDM = dnevniMeni.getText();
        try{
            Database.updateTextField(newDM, "dnevni_meni", korisnik);
            logger.info("Uspješno promijenjen dnevni meni.");
        }catch (SQLException | IOException e){
            e.printStackTrace();
            logger.error("Greška pri promijeni dnevnog menija.");
        }
    }

    public void saveRVChanges() {
        radnoVrijeme.setEditable(false);
        saveRV.setVisible(false);
        editRVButton.setVisible(true);
        String newRV = radnoVrijeme.getText();
        try{
            Database.updateTextField(newRV, "radno_vrijeme", korisnik);
            logger.info("Uspješno promijenjeno radno vrijeme.");
        }catch (SQLException | IOException e){
            e.printStackTrace();
            logger.error("Greška pri promijeni radnog vremena.");
        }
    }

    public void showMenu(){
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("meni-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Uspješno postavljena scena za prikaz jelovnika.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz jelovnika.");
        }
        stage.setTitle("Jelovnik");
        stage.setScene(scene);
        stage.show();
        meniStage = stage;
    }

    public static Stage getMeniStage(){
        return meniStage;
    }

    public static void setPosjecenost(Long p){
        posjecenost = p;
    }
}

