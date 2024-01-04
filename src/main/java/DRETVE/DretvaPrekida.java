package DRETVE;

import com.example.menze.MainController;
import com.example.menze.MenzaViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;


public class DretvaPrekida implements Runnable{
    Timeline prikazPosjecenosti;
    @Override
    public void run() {
        /*prikazPosjecenosti = new Timeline( new KeyFrame(Duration.seconds(4),
                event -> {
                    Platform.runLater(new DretvaPosjecenosti());
                }));
        prikazPosjecenosti.setCycleCount(Timeline.INDEFINITE);
        prikazPosjecenosti.play();*/

        MainController.getMenzaStage().setOnCloseRequest(windowEvent -> {
            //prikazPosjecenosti.stop();
            MenzaViewController.getPrikazPosjecenosti().stop();
        });
        System.out.println("Ranala sam se");
    }
}
