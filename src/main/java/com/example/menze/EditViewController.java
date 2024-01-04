package com.example.menze;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Jelo;

import java.awt.*;
import java.io.IOException;

public class EditViewController {
    @FXML
    private TextField name;
    @FXML
    private TextField price;

    @FXML
    public void initialize(){
        Jelo jelo = MeniViewController.getJeloZaUredit();
        name.setText(jelo.getNaziv());
        price.setText(jelo.getCijena().toString());
    }
    public void save() {
        String newName = name.getText();
        String newPrice = price.getText();
        Jelo jelo = new Jelo(99, newName, Double.valueOf(newPrice));
        MeniViewController.setJeloZaUredit(jelo);
        MeniViewController.updateGJ();
        Stage thisStage = (Stage) name.getScene().getWindow();
        thisStage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("meni-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(MainController.getBundle());
            scene = new Scene(fxmlLoader.load(), 640, 480);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MenzaViewController.getMeniStage().setScene(scene);
        MenzaViewController.getMeniStage().show();
    }

    public void delete(){
        MeniViewController.deleteGJ();
        Stage thisStage = (Stage) name.getScene().getWindow();
        thisStage.close();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("meni-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(MainController.getBundle());
            scene = new Scene(fxmlLoader.load(), 640, 480);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MenzaViewController.getMeniStage().setScene(scene);
        MenzaViewController.getMeniStage().show();
    }
}
