package com.example.menze;

import DRETVE.DownloadTask;
import database.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Jelo;
import model.Korisnik;
import model.Menza;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.json.simple.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MeniViewController {
    @FXML
    private Label naziv;
    @FXML
    private TableView<Jelo> gj;
    @FXML
    private TableColumn<Jelo, String> gjNaziv;
    @FXML
    private TableColumn<Jelo, String> gjCijena;
    @FXML
    private TableView<Jelo> p;
    @FXML
    private TableColumn<Jelo, String> pNaziv;
    @FXML
    private TableColumn<Jelo, String> pCijena;
    @FXML
    private TableView<Jelo> o;
    @FXML
    private TableColumn<Jelo, String> oNaziv;
    @FXML
    private TableColumn<Jelo, String> oCijena;
    @FXML
    private ComboBox<String> jeloCB;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ComboBox<String> speedCB;


    private Menza menza;
    private Korisnik korisnik = MainController.getK();
    private static File glavnoJelo = new File("dat/glavna_jela.xml");
    private static File prilog = new File("dat/prilozi.json");
    private File ostalo  = new File("dat/ostala.bin");
    private static List<Jelo> glavnaJela;
    private List<Jelo> prilozi = new ArrayList<>();
    private List<Jelo> ostalaJela = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(MeniViewController.class);
    private static ResourceBundle bundle = MainController.getBundle();


    @FXML
    public void initialize() throws ParserConfigurationException, IOException, SAXException {
        logger.info("Example log from {}", MeniViewController.class.getSimpleName());
        glavnaJela = new ArrayList<>();

        Integer id;
        String line, nazivJela;
        String cijenaJela;
        boolean vege;
        if(MainController.getPrijavljeno())
        {
            try{
                menza = Database.getMenzaFromKorisnik(korisnik);
                logger.info("Uspješno dohvaćena menza iz baze podataka.");
            }catch (SQLException | IOException e){
                e.printStackTrace();
                logger.error("Greška pri dohvaćanju menze iz baze podataka.");
            }
        }else
            menza = SveMenzeViewController.getMenza();

        naziv.setText("Jelovnik - " + menza.getNaziv());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document glavnaJelaDoc = db.parse(glavnoJelo);
        glavnaJelaDoc.getDocumentElement().normalize();
        NodeList listaGlavnihJela = glavnaJelaDoc.getElementsByTagName("glavnoJelo");
        for(int i = 0; i<listaGlavnihJela.getLength(); i++){
            Node node = listaGlavnihJela.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                Element eElement = (Element) node;
                id = Integer.valueOf(eElement.getElementsByTagName("id").item(0).getTextContent());
                nazivJela = eElement.getElementsByTagName("naziv").item(0).getTextContent();
                cijenaJela = eElement.getElementsByTagName("cijena").item(0).getTextContent();
                vege = eElement.getElementsByTagName("vege").item(0).getTextContent().equals("true");
                if(vege)
                    nazivJela = nazivJela.concat(" (vege)");
                glavnaJela.add(new Jelo(id, nazivJela, Double.valueOf(cijenaJela)));
            }
        }

        gjNaziv.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNaziv()));
        gjCijena.setCellValueFactory(cd -> new SimpleStringProperty(
                BigDecimal.valueOf(cd.getValue().getCijena()).setScale(2, RoundingMode.HALF_UP) + "€"));
        gj.setItems(FXCollections.observableList(glavnaJela));

        JSONParser jsonParser = new JSONParser();
        try(FileReader prilogReader = new FileReader(prilog)){
            Object obj = jsonParser.parse(prilogReader);
            JSONArray prilogi = (JSONArray) obj;
            prilogi.forEach(pr->prilozi.add(parsePrilog((JSONObject) pr)));
            logger.info("Uspješno učitan čitač datoteke za priloge.");
        }catch (IOException | ParseException e){
            e.printStackTrace();
            logger.error("Greška pri učitavanju čitača datoteke za priloge.");
        }

        pNaziv.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNaziv()));
        pCijena.setCellValueFactory(cd -> new SimpleStringProperty(
                BigDecimal.valueOf(cd.getValue().getCijena()).setScale(2, RoundingMode.HALF_UP) + "€"));
        p.setItems(FXCollections.observableList(prilozi));

        try(FileInputStream fis = new FileInputStream(ostalo); ObjectInputStream ois = new ObjectInputStream(fis)){
            while(true) {
                try {
                    ostalaJela = (List<Jelo>) ois.readObject();
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        oNaziv.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNaziv()));
        oCijena.setCellValueFactory(cd -> new SimpleStringProperty(
                BigDecimal.valueOf(cd.getValue().getCijena())
                        .setScale(2, RoundingMode.HALF_UP) + "€"));
        o.setItems(FXCollections.observableList(ostalaJela));

        List<String> kategorijeJela = new ArrayList<>();
        kategorijeJela.add("Glavna jela");
        kategorijeJela.add("Prilozi");
        kategorijeJela.add("Ostalo");
        jeloCB.setItems(FXCollections.observableList(kategorijeJela));
        List<String> speed = new ArrayList<>();
        speed.add("1 KB/s");
        speed.add("250 B/s");
        speed.add("130 B/s");
        speedCB.setItems(FXCollections.observableList(speed));
        progressBar.setVisible(false);
    }
    private static Jelo parsePrilog(JSONObject pr){
        JSONObject jo = (JSONObject) pr.get("prilog");
        Integer id = ((Long) jo.get("id")).intValue();
        String naziv = (String) jo.get("naziv");
        String cijena = (String) jo.get("cijena");
        return new Jelo(id, naziv, Double.valueOf(cijena));
    }

    private static Stage iNStage;

    public void insertNew(){
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("insert-new-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 216, 240);
            logger.info("Uspješno postavljena scena za prikaz ekrana za unos novog jela.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za unos novog jela.");
        }
        stage.setTitle("Insert new");
        stage.setScene(scene);
        stage.show();
        iNStage = stage;
    }

    private Integer spid = 0;
    public void skini(){
        String odabrano = jeloCB.getValue();
        String odabranoSpeed = speedCB.getValue();
        String fileUrl = "";
        String saveAt  = "";

        boolean gogo = true;
        jeloCB.setStyle("-fx-border-color: none");
        progressBar.progressProperty().setValue(0);

        switch (odabrano){
            case "Glavna jela" ->{
                fileUrl = "http://localhost:8081/rest/jelovnik/glavna_jela.xml";
                saveAt = "dat/glavna_jela_v2.xml";
            }
            case "Prilozi" ->{
                fileUrl = "http://localhost:8081/rest/jelovnik/prilozi.json";
                saveAt = "dat/prilozi_v2.json";
            }
            case "Ostalo" ->{
                fileUrl = "http://localhost:8081/rest/jelovnik/ostala.bin";
                saveAt = "dat/ostala_v2.bin";
            }
            default ->{
                gogo = false;
                jeloCB.setStyle("-fx-border-color: red");
            }
        }
        switch (odabranoSpeed){
            case "1 KB/s" ->{
                spid = 1024;
            }
            case "250 B/s" ->{
                spid = 256;
            }
            case "130 B/s" ->{
                spid = 128;
            }
            default ->{
                gogo = false;
                speedCB.setStyle("-fx-border-color: red");
            }
        }
        if(gogo){
            progressBar.setVisible(true);
            Task<Void> task = new DownloadTask(fileUrl, saveAt, spid, this);
            //progressBar.progressProperty().bind(task.progressProperty());
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void updateProgressBar(double progress){
        progressBar.progressProperty().setValue(progress);
    }


    public static Stage getiNStage(){
        return iNStage;
    }

    public static Jelo jeloZaUredit;

    public static Jelo getJeloZaUredit(){
        return jeloZaUredit;
    }

    public static void setJeloZaUredit(Jelo j){
        jeloZaUredit.setNaziv(j.getNaziv());
        jeloZaUredit.setCijena(j.getCijena());
    }

    public static void updateGJ(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document glavnaJelaDoc = null;
        try {
            glavnaJelaDoc = db.parse(glavnoJelo);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        glavnaJelaDoc.getDocumentElement().normalize();
        NodeList listaGlavnihJela = glavnaJelaDoc.getElementsByTagName("glavnoJelo");
        for(int i = 0; i<listaGlavnihJela.getLength(); i++) {
            Node node = listaGlavnihJela.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if(Integer.valueOf(eElement.getElementsByTagName("id").item(0).getTextContent()).equals(jeloZaUredit.getId()))
                {
                    Node name = eElement.getElementsByTagName("naziv").item(0).getFirstChild();
                    name.setNodeValue(jeloZaUredit.getNaziv());
                    Node cijena = eElement.getElementsByTagName("cijena").item(0).getFirstChild();
                    cijena.setNodeValue(jeloZaUredit.getCijena().toString());
                }
            }
        }
        writeXmlDocumentToFile(glavnaJelaDoc, glavnoJelo);
    }

    public static void deleteGJ(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document glavnaJelaDoc = null;
        try {
            glavnaJelaDoc = db.parse(glavnoJelo);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        glavnaJelaDoc.getDocumentElement().normalize();
        NodeList listaGlavnihJela = glavnaJelaDoc.getElementsByTagName("glavnoJelo");
        for(int i = 0; i<listaGlavnihJela.getLength(); i++) {
            Node node = listaGlavnihJela.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (Integer.valueOf(eElement.getElementsByTagName("id").item(0).getTextContent()).equals(jeloZaUredit.getId())) {
                    eElement.getParentNode().removeChild(eElement);
                }
            }
        }
        writeXmlDocumentToFile(glavnaJelaDoc, glavnoJelo);
    }

    public void editGJ() {
        jeloZaUredit = gj.getSelectionModel().getSelectedItem();
        showEdit();
    }
    private static void writeXmlDocumentToFile(Document xmlDocument, File gj) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            FileOutputStream outStream = new FileOutputStream(gj);
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(outStream));
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* public void editP() {
        jeloZaUredit = p.getSelectionModel().getSelectedItem();
        showEdit();
    }

    public static void updateP(){
        JSONParser jsonParser = new JSONParser();
        List<Jelo> stariPrilozi = new ArrayList<>();
        try(FileReader prilogReader = new FileReader(prilog)){
            Object obj = jsonParser.parse(prilogReader);
            JSONArray prilogi = (JSONArray) obj;
            prilogi.forEach(pr->stariPrilozi.add(parsePrilog((JSONObject) pr)));
            logger.info("Uspješno učitan čitač datoteke za priloge.");
        }catch (IOException | ParseException e){
            e.printStackTrace();
            logger.error("Greška pri učitavanju čitača datoteke za priloge.");
        }
        stariPrilozi.stream().forEach(p-> {
            if(p.getId().equals(jeloZaUredit.getId())){
                stariPrilozi.add(stariPrilozi.indexOf(p), jeloZaUredit);
            }
        });

    }

    public static void deleteP(){

    }*/

    private void showEdit(){
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("edit-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 211, 175);
            logger.info("Postavljena scena za prikaz ekrana za prijavu.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz ekrana za prijavu.");
        }
        stage.setTitle("Edit");
        stage.setScene(scene);
        stage.show();
    }
}
