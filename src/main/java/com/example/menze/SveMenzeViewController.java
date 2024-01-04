package com.example.menze;

import database.Database;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.GradoviComparator;
import model.Menza;
import model.NaziviComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SveMenzeViewController {
    private Scene preScene;
    @FXML
    private TableView<Menza> menzaTableView;
    @FXML
    private TableColumn<Menza, String> nazivColumn;
    @FXML
    private TableColumn<Menza, String> gradColumn;
    @FXML
    private TableColumn<Menza, Boolean> favoritColumn;
    @FXML
    private ComboBox<String> prviSort;
    @FXML
    private ComboBox<String> drugiSort;
    @FXML
    private TextField drugiSortSearch;
    @FXML
    private Label drugiSortLabel;

    private CheckBox checkBox;

    private List<Menza> menze = new ArrayList<>();
    private static Menza menza;
    private static final Logger logger = LoggerFactory.getLogger(SveMenzeViewController.class);
    private static boolean prijavljenaMenza = false;

    @FXML
    public void initialize(){
        logger.info("Example log from {}", SveMenzeViewController.class.getSimpleName());

        try {
            menze = Database.getSveMenze();
            logger.info("Uspješno dohvaćene menze iz baze podataka.");
        }catch (SQLException | IOException e)
        {
            e.printStackTrace();
            logger.error("Greška kod dohvaćanja menzi iz baze podataka.");
        }

        nazivColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getNaziv());
        });
        /*nazivColumn.setCellFactory(tc -> {
                    TableCell<Menza, String> cell = new TableCell<Menza, String>();
                    cell.setOnMouseClicked(event -> {
                        if (!cell.isEmpty()) {
                            menza = cell.getTableRow().getItem();
                            try {
                                MainController.showMenza();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return cell;
                });

        gradColumn.setCellFactory( tc -> {
            TableCell<Menza, String> cell = new TableCell<Menza, String>();
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    menza = cell.getTableRow().getItem();
                    try {
                        MainController.showMenza();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            return cell;
        });*/
        gradColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGrad()));
        favoritColumn.setCellValueFactory(cellData -> cellData.getValue().getFavorit());
        favoritColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        if(MainController.getPrijavljeno())
            favoritColumn.setVisible(true);

        ObservableList<Menza> menzaObservableList = FXCollections.observableList(menze);
        menzaTableView.setItems(menzaObservableList);
        favoritColumn.setEditable(true);

        menzaTableView.setRowFactory(tv-> {
            TableRow<Menza> row = new TableRow<>();
            row.setOnMouseClicked( event -> {
                if (!row.isEmpty())
                {
                    menza = row.getItem();
                    if(MainController.getPrijavljeno())
                        prijavljenaMenza=true;
                    else
                        prijavljenaMenza=false;
                    MainController.setOtvorioSveMenze(true);
                    MainController.showMenza();

                }
            });
            return row;
        });

        List<String> kategorije = new ArrayList<>();
        kategorije.add("Naziv");
        kategorije.add("Grad");
        ObservableList<String> observableKategorije = FXCollections.observableList(kategorije);
        prviSort.setItems(observableKategorije);
    }

    public static boolean getPrijavljenoMenza(){
        return prijavljenaMenza;
    }

    public void sort1(){
        if(prviSort.getValue().equals("Grad")){
            drugiSort.setVisible(true);
            drugiSortSearch.setVisible(false);
            try{
                List<String> gradovi = Database.getGradovi();
                gradovi.add("(bez odabira)");
                ObservableList observableGradovi = FXCollections.observableList(gradovi.stream().distinct().toList());
                drugiSort.setItems(observableGradovi);
                menzaTableView.setItems(FXCollections.observableList(menze.stream().
                        sorted(new GradoviComparator()).collect(Collectors.toList())));
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }

        }
        else if(prviSort.getValue().equals("Naziv")){
            drugiSortSearch.setVisible(true);
            drugiSort.setVisible(false);

            menzaTableView.setItems(FXCollections.observableList(menze.stream().
                    sorted(new NaziviComparator()).collect(Collectors.toList())));
        }
    }

    public void sort2() {
        List<Menza> sortedList;
        String drugiSortText;
        if (prviSort.getValue().equals("Grad")) {
            drugiSortText = drugiSort.getValue();
            sortedList = menze.stream().sorted(new GradoviComparator()).collect(Collectors.toList());
            if(drugiSortText.equals("(bez odabira)")){
                menzaTableView.setItems(FXCollections.observableList(sortedList));
            }
            else
            {
                menzaTableView.setItems(FXCollections.observableList(sortedList.stream().
                        filter(m -> m.getGrad().equals(drugiSortText)).collect(Collectors.toList())));
            }
        }
        else{
            drugiSortText = drugiSortSearch.getText();
            sortedList = menze.stream().sorted(new NaziviComparator()).collect(Collectors.toList());
            menzaTableView.setItems(FXCollections.observableList(sortedList.stream().
                    filter(m -> m.getNaziv().toLowerCase().contains(drugiSortText.toLowerCase())).collect(Collectors.toList())));
        }
    }

    public static Menza getMenza(){
        return menza;
    }

    public void setPreScene(Scene preScene){
        this.preScene = preScene;
    }

    public void returnButton(){
        Stage stage = MainApplication.getStage();
        stage.setScene(preScene);
        stage.show();
    }
}
