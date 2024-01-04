package com.example.menze;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import database.Database;
import jakarta.ws.rs.client.ClientBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Jelo;
import model.Menza;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.net.URI;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.awt.Desktop;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;


public class IzdavanjeRacunaViewController {

    @FXML
    private ComboBox<String> jelaCB;
    @FXML
    private Spinner<Integer> kolicina;
    @FXML
    private TextArea racun;
    @FXML
    private CheckBox subvencionirano;

    private Scene preScene;
    private File glavnoJelo = new File("dat/glavna_jela.xml");
    private File prilog = new File("dat/prilozi.json");
    private File ostalo  = new File("dat/ostala.bin");
    private List<Jelo> dailyMenu = new ArrayList<>();
    public void setPreScene(Scene preScene){
        this.preScene = preScene;
    }

    private Double suma = 0.0;

    public void returnButton(){
        Stage stage = MainApplication.getStage();
        stage.setScene(preScene);
        stage.show();
    }

    public void initialize(){
        kolicina.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1,3));
        kolicina.getValueFactory().setValue(1);
        List<Jelo> fullMenu = fullMenu();
        dailyMenu = getJelaFromJelovnik(fullMenu);
        ObservableList<String> naziviJelaDM = FXCollections.observableArrayList();
        for(Jelo j:dailyMenu)
            naziviJelaDM.add(j.getNaziv());
        jelaCB.setItems(naziviJelaDM);
    }

    public void dodajJelo(){
        String odabranoJeloNaziv = jelaCB.getValue();
        Jelo odabranoJelo = dailyMenu.stream().filter(jelo -> jelo.getNaziv().equalsIgnoreCase(odabranoJeloNaziv)).findFirst().get();
        Integer kol = kolicina.getValue();
        boolean noncontinuar = false;
        DecimalFormat df = new DecimalFormat("#,##");

        switch (kol){
            case 1 -> {
                racun.appendText(odabranoJelo.getNaziv() + " x1 - ");
                if(subvencionirano.isSelected()) {
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena()).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena();
                }
                else
                {
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena() / 0.29).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena()/0.29;
                }
            }
            case 2 -> {
                racun.appendText(odabranoJelo.getNaziv() + " x2 - ");
                if(subvencionirano.isSelected()){
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena() *2).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena()*2;
                }
                else
                {
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena() / 0.29*2).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena()/0.29*2;
                }
            }
            case 3 ->{
                racun.appendText(odabranoJelo.getNaziv() + " x3 - ");
                if(subvencionirano.isSelected())
                {
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena() *3).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena()*3;
                }
                else
                {
                    racun.appendText(BigDecimal.valueOf(odabranoJelo.getCijena() / 0.29*3).setScale(2, RoundingMode.HALF_UP) + "€\n");
                    suma += odabranoJelo.getCijena()/0.29*3;
                }
            }
        }

    }

    private List<Jelo> getJelaFromJelovnik(List<Jelo> fullMenu){
        List<Jelo> jela = new ArrayList<>();
        List<String> naziviJela = new ArrayList<>();
        String jelovnik = "", line;
        try {
            jelovnik = Database.getJelovnik(MainController.getK());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        try(BufferedReader reader = new BufferedReader(new StringReader(jelovnik))) {
            while ((line = reader.readLine()) != null){
                if(!(line.equals("Glavna jela:") || line.equals("Prilozi:") || line.equals("Ostalo:")))
                {
                    int ojro = line.indexOf('€');
                    naziviJela.add(line.substring(0, ojro-7));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String naziv: naziviJela){
            jela.add(fullMenu.stream().filter(jelo -> jelo.getNaziv().equalsIgnoreCase(naziv)).findAny().get());
        }

        return jela;
    }

    private List<Jelo> fullMenu(){
        List<Jelo> fm = new ArrayList<>();
        Integer id;
        String line, nazivJela, cijenaJela;
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
                    fm.add(new Jelo(id, nazivJela, Double.valueOf(cijenaJela)));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        JSONParser jsonParser = new JSONParser();
        try(FileReader prilogReader = new FileReader(prilog)){
            Object obj = jsonParser.parse(prilogReader);
            JSONArray prilogi = (JSONArray) obj;
            prilogi.forEach(pr->fm.add(parsePrilog((JSONObject) pr)));
        }catch (IOException | ParseException e){
            e.printStackTrace();
        }

        List<Jelo> ostalaJela = new ArrayList<>();
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
        fm.addAll(ostalaJela);

        return fm;
    }

    private static Jelo parsePrilog(JSONObject pr){
        JSONObject jo = (JSONObject) pr.get("prilog");
        Integer id = ((Long) jo.get("id")).intValue();
        String naziv = (String) jo.get("naziv");
        String cijena = (String) jo.get("cijena");
        return new Jelo(id, naziv, Double.valueOf(cijena));
    }

    Client client;
    public void izdajRacun(){
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try{
            PdfWriter.getInstance(document, new FileOutputStream("dat/racun.pdf"));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Menza menza = null;
        try{
            menza = Database.getMenzaFromKorisnik(MainController.getK());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        Paragraph p1 = null;
        if(menza != null){
            p1 = new Paragraph(menza.getNaziv(), font);
            p1.add(new Paragraph(" "));
            p1.add(new Paragraph(menza.getAdresa() + ", "+ menza.getGrad(), font));
            p1.add(new Paragraph(" "));
            p1.add(new Paragraph(racun.getText(),font));
            p1.add(new Paragraph(" "));
            p1.add(new Paragraph("\t\t Ukupno: " +
                    BigDecimal.valueOf(suma).setScale(2, RoundingMode.HALF_UP) + "€", font));
            suma=0.0;
            client = ClientBuilder.newClient();
            URI uri = URI.create("http://localhost:8081/rest/danas");
            Response response = client.target(uri).request().get();
            String datum = response.readEntity(String.class);
            p1.add(new Paragraph("\n Izdano: " + datum, font));
        }

        try {
            document.add(p1);
            racun.clear();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();

        try{
            Desktop.getDesktop().open(new File("dat/racun.pdf"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
