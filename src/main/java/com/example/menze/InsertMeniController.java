package com.example.menze;

import database.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.Jelo;
import model.Korisnik;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class InsertMeniController{
    private File glavnoJelo = new File("dat/glavna_jela.xml");
    private File prilog = new File("dat/prilozi.json");
    private File ostalo  = new File("dat/ostala.bin");
    private List<Jelo> glavnaJela = new ArrayList<>();
    private List<Jelo> prilozi = new ArrayList<>();
    private List<Jelo> ostalaJela = new ArrayList<>();
    private Jelo odabranoJelo;
    private Integer indexGJ = 13, indexP = 22,  indexO = 29;
    private static final Logger logger = LoggerFactory.getLogger(InsertMeniController.class);
    private static ResourceBundle bundle = MainController.getBundle();


    private Scene preScene = MenzaViewController.getPreScene();


    @FXML
    private ComboBox<String> gj;
    @FXML
    private ComboBox<String> p;
    @FXML
    private ComboBox<String> o;
    @FXML
    private TextArea preview;



    @FXML
    public void initialize(){
        logger.info("Example log from {}", InsertMeniController.class.getSimpleName());

        Integer id;
        String line, nazivJela;
        String cijenaJela;
        List<String> naziviGJ = new ArrayList<>(), naziviP = new ArrayList<>(), naziviO = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document glavnaJelaDoc = db.parse(glavnoJelo);
            glavnaJelaDoc.getDocumentElement().normalize();
            NodeList listaGlavnihJela = glavnaJelaDoc.getElementsByTagName("glavnoJelo");
            for (int i = 0; i < listaGlavnihJela.getLength(); i++) {
                Node node = listaGlavnihJela.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
                    id = Integer.valueOf(eElement.getElementsByTagName("id").item(0).getTextContent());
                    nazivJela = eElement.getElementsByTagName("naziv").item(0).getTextContent();
                    cijenaJela = eElement.getElementsByTagName("cijena").item(0).getTextContent();
                    glavnaJela.add(new Jelo(id, nazivJela, Double.valueOf(cijenaJela)));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        glavnaJela.forEach(jelo -> naziviGJ.add(jelo.getNaziv()));
        gj.setItems(FXCollections.observableList(naziviGJ));

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
        prilozi.forEach(jelo -> naziviP.add(jelo.getNaziv()));
        p.setItems(FXCollections.observableList(naziviP));


        try(FileInputStream fis = new FileInputStream(ostalo);
            ObjectInputStream ois = new ObjectInputStream(fis)){
            while(true) {
                try {
                    ostalaJela = (List<Jelo>) ois.readObject();
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        ostalaJela.forEach(jelo -> naziviO.add(jelo.getNaziv()));
        o.setItems(FXCollections.observableList(naziviO));

        preview.setText("Glavna jela:\nPrilozi:\nOstalo:");
    }

    public void odabranoGlavnoJelo(){
        String odabranoJeloNaziv = gj.getValue();
        glavnaJela.forEach(jelo -> {
            if(jelo.getNaziv().equals(odabranoJeloNaziv))
                odabranoJelo = jelo;
        });
        String appendix = odabranoJelo.getNaziv() + " - " +
                BigDecimal.valueOf(odabranoJelo.getCijena()).setScale(2, RoundingMode.HALF_UP) + "€\n";
        preview.insertText(indexGJ, appendix);
        indexGJ += appendix.length();
        indexP += appendix.length();
        indexO += appendix.length();
    }

    public void odabranPrilog(){
        String odabranoJeloNaziv = p.getValue();
        prilozi.forEach(jelo -> {
            if(jelo.getNaziv().equals(odabranoJeloNaziv))
                odabranoJelo = jelo;
        });
        String appendix =odabranoJelo.getNaziv() + " - " +
                BigDecimal.valueOf(odabranoJelo.getCijena()).setScale(2, RoundingMode.HALF_UP) + "€\n";
        preview.insertText(indexP, appendix);
        indexP += appendix.length();
        indexO += appendix.length();
    }

    public void odabranoOstalo(){
        String odabranoJeloNaziv = o.getValue();
        ostalaJela.forEach(jelo -> {
            if(jelo.getNaziv().equals(odabranoJeloNaziv))
                odabranoJelo = jelo;
        });
        String appendix ="\n" + odabranoJelo.getNaziv() + " - " +
                BigDecimal.valueOf(odabranoJelo.getCijena()).setScale(2, RoundingMode.HALF_UP) + "€";
        preview.insertText(indexO, appendix);
        indexO += appendix.length();
    }

    public void spremi() {
        Korisnik korisnik = MainController.getK();
        String dnevniMeni = preview.getText();
        try{
            Database.insertDnevniMeni(dnevniMeni, korisnik);
            logger.info("Uspjesno unesen dnevni meni u bazu podataka.");
        }catch (IOException | SQLException e){
            e.printStackTrace();
            logger.error("Greška kod unosa dnevnog menija u bazu podataka.");
        }
        MenzaViewController.getInsertMeniStage().close();
        Stage stage = MainApplication.getStage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("menza-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load(), 640, 480);
            logger.info("Uspješno postavljena scena za prikaz ekrana menze za koju smo unosili dnevni meni.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška kod postavljanja scene za prikaz ekrana menze za koju smo unosili dnevni meni.");
        }
        MenzaViewController controller = fxmlLoader.getController();
        controller.setPreScene(preScene);
        stage.setTitle("Jelovnik");
        stage.setScene(scene);
        stage.show();
    }
    private static Jelo parsePrilog(JSONObject pr){
        JSONObject jo = (JSONObject) pr.get("prilog");
        Integer id = ((Long) jo.get("id")).intValue();
        String naziv = (String) jo.get("naziv");
        String cijena = (String) jo.get("cijena");
        return new Jelo(id, naziv, Double.valueOf(cijena));
    }
}
