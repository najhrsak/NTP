package com.example.menze;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Jelo;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class InsertNewController {
    @FXML
    private ComboBox<String> kategorije;
    @FXML
    private TextField naziv;
    @FXML
    private TextField cijena;
    @FXML
    private Label poruka;
    @FXML
    private CheckBox vege;
    @FXML
    private ChoiceBox<String> subvencijaCB;
    @FXML
    private Label subvencija;


    private final List<String> listaKategorija = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(InsertNewController.class);
    private static ResourceBundle bundle = MainController.getBundle();
    private static File glavnaJela = new File("dat/glavna_jela.xml");
    private static File prilog = new File("dat/prilozi.json");
    private static File ostala = new File("dat/ostala.bin");

    @FXML
    public void initialize(){
        logger.info("Example log from {}", InsertNewController.class.getSimpleName());

        listaKategorija.add("glavna_jela");
        listaKategorija.add("prilozi");
        listaKategorija.add("ostalo");

        kategorije.setItems(FXCollections.observableList(listaKategorija));
        kategorije.getSelectionModel().selectFirst();

        ObservableList ol = FXCollections.observableList(List.of("0.71", "0.50"));
        subvencijaCB.setItems(ol);
    }

    public void unsesi() {
        Integer id = 0;
        String uneseniNaziv, unesenaCijena;
        uneseniNaziv = naziv.getText();
        unesenaCijena = cijena.getText();
        if(unesenaCijena.equals("") || uneseniNaziv.equals("")){
            if(unesenaCijena.equals(""))
                cijena.setStyle("-fx-border-color: red");
            else
                cijena.setStyle(null);
            if(uneseniNaziv.equals(""))
                naziv.setStyle("-fx-border-color: red");
            else
                naziv.setStyle(null);
            poruka.setVisible(true);
        }
        else {
            poruka.setVisible(false);
            switch (kategorije.getValue()) {
                case "glavna_jela" -> {
                    try {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document glavnaJelaDoc = db.parse(glavnaJela);
                        glavnaJelaDoc.getDocumentElement().normalize();
                        NodeList listaGlavnihJela = glavnaJelaDoc.getElementsByTagName("glavnoJelo");
                        Node node = listaGlavnihJela.item(listaGlavnihJela.getLength()-1);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) node;
                            id = Integer.valueOf(eElement.getElementsByTagName("id").item(0).getTextContent());
                            //jaxbToXML(new Jelo(id+1, uneseniNaziv, unesenaCijena));
                        }

                        Element novoJelo = glavnaJelaDoc.createElement("glavnoJelo");
                        Element noviid = glavnaJelaDoc.createElement("id");
                        noviid.appendChild(glavnaJelaDoc.createTextNode(String.valueOf(id+1)));
                        novoJelo.appendChild(noviid);
                        Element noviNaziv = glavnaJelaDoc.createElement("naziv");
                        noviNaziv.appendChild(glavnaJelaDoc.createTextNode(uneseniNaziv));
                        novoJelo.appendChild(noviNaziv);
                        Element novaCijena = glavnaJelaDoc.createElement("cijena");
                        novaCijena.appendChild(glavnaJelaDoc.createTextNode(unesenaCijena));
                        novoJelo.appendChild(novaCijena);
                        Element novoVege = glavnaJelaDoc.createElement("vege");
                        novoVege.appendChild(glavnaJelaDoc.createTextNode(String.valueOf(vege.isSelected())));
                        novoJelo.appendChild(novoVege);
                        glavnaJelaDoc.getDocumentElement().appendChild(novoJelo);

                        writeXmlDocumentToFile(glavnaJelaDoc, glavnaJela);
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                    }
                }
                case "prilozi" -> {
                    JSONParser jsonParser = new JSONParser();
                    JSONArray prilogi = new JSONArray();
                    try(FileReader prilogReader = new FileReader(prilog)){
                        Object obj = jsonParser.parse(prilogReader);
                        prilogi = (JSONArray) obj;

                    }catch (IOException | ParseException e){
                        e.printStackTrace();
                    }
                    id = getLastId((JSONObject) prilogi.get(prilogi.size()-1));
                    JSONObject jo = new JSONObject();
                    jo.put("id", id);
                    jo.put("naziv", uneseniNaziv);
                    jo.put("cijena", unesenaCijena);
                    JSONObject jo2 = new JSONObject();
                    jo2.put("prilog", jo);
                    prilogi.add(jo2);
                    try(FileWriter prilogWriter = new FileWriter(prilog)){
                        prilogWriter.write(prilogi.toJSONString());
                        prilogWriter.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                case "ostalo" -> {
                    Double cijena = Double.valueOf(unesenaCijena) - Double.valueOf(unesenaCijena) * Double.valueOf(subvencijaCB.getValue());
                    Jelo unesenoJelo = new Jelo(id, uneseniNaziv, cijena);
                    List<Jelo> ostalaJela = new ArrayList<>();
                    try(FileInputStream fis = new FileInputStream(ostala);
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
                    ostalaJela.add(unesenoJelo);

                    try(FileOutputStream fos = new FileOutputStream(ostala);
                        ObjectOutputStream oos = new ObjectOutputStream(fos)){
                        oos.writeObject(ostalaJela);
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            MeniViewController.getiNStage().close();
            Stage stage = MenzaViewController.getMeniStage();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("meni-view.fxml"));
            Scene scene = null;
            try {
                fxmlLoader.setResources(bundle);
                scene = new Scene(fxmlLoader.load(), 640, 480);
                logger.info("Uspješno postavljena scene za prikaz menze iz koje smo učitali unos novog jela.");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Greška pri postavljanju scene za prikaz menze iz koje smo učitali unos novog jela.");
            }
            stage.setTitle("Jelovnik");
            stage.setScene(scene);
            stage.show();

        }
    }

    private void writeXmlDocumentToFile(Document xmlDocument, File gj) {
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

    private static int getLastId(JSONObject pr){
        JSONObject jo = (JSONObject) pr.get("prilog");
        return ((Long) jo.get("id")).intValue();
    }


    public void struktura() {
        if(kategorije.getValue().equalsIgnoreCase("glavna_jela")){
            vege.setVisible(true);
            subvencija.setVisible(false);
            subvencijaCB.setVisible(false);
        }
        else if(kategorije.getValue().equalsIgnoreCase("prilozi")){
            vege.setVisible(false);
            subvencija.setVisible(false);
            subvencijaCB.setVisible(false);
        }
        else if(kategorije.getValue().equalsIgnoreCase("ostalo")){
            vege.setVisible(false);
            subvencija.setVisible(true);
            subvencijaCB.setVisible(true);
        }
    }
}
