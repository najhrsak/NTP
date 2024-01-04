package com.example.menze;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PopupController {
    @FXML
    private Label lable;
    private static Stage stage;

    public void da() {
        stage = (Stage) lable.getScene().getWindow();
        MainController.setDaNe(true);
        stage.close();
    }
    public void ne(){
        stage = (Stage) lable.getScene().getWindow();
        MainController.setDaNe(false);
        stage.close();
    }
    public static Stage getPopupStage(){
        return stage;
    }

}
