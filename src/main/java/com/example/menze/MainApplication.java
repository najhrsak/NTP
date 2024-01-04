package com.example.menze;

import DRETVE.DretvaPosjecenosti;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.*;
import java.security.*;
import java.util.Locale;
import java.util.ResourceBundle;
import org.ini4j.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainApplication extends Application {
    private static Stage mainStage;
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    private Ini iniRead;
    private Ini iniWrite;
    private double width, height, x, y;



    @Override
    public void start(Stage stage) throws IOException {
        /*PublicKey publicKey;
        PrivateKey privateKey;
        try{
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (FileOutputStream fos = new FileOutputStream("dat/public.key")) {
            fos.write(publicKey.getEncoded());
        }
        try (FileOutputStream fos = new FileOutputStream("dat/private.key")) {
            fos.write(privateKey.getEncoded());
        }*/
        /*SecretKey key;

        try{
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey();
            char[] hex = encodeHex(key.getEncoded());
            writeStringToFile(new File("dat/aes_key.key"), String.valueOf(hex));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }*/


        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("first-view.fxml"));
        Scene scene = null;
        try {
            fxmlLoader.setResources(bundle);
            scene = new Scene(fxmlLoader.load());
            logger.info("Uspješno postavljena scena za prikaz početnog zaslona.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Greška pri postavljanju scene za prikaz početnog zaslona.");
        }
        iniRead = new Ini();
        iniRead.load(new FileReader("dat/settings.ini"));
        width = iniRead.get("scene", "width", double.class);
        height = iniRead.get("scene", "height", double.class);
        x = iniRead.get("scene", "x", double.class);
        y = iniRead.get("scene", "y", double.class);

        stage.setWidth(width);
        stage.setHeight(height);
        stage.setY(y);
        stage.setX(x);
        stage.setTitle("Menze");
        stage.setScene(scene);
        stage.show();
        mainStage = stage;

        mainStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        iniWrite = new Ini();
                        iniWrite.put("scene", "width", mainStage.getWidth());
                        iniWrite.put("scene", "height", mainStage.getHeight());
                        iniWrite.put("scene", "x", mainStage.getX());
                        iniWrite.put("scene", "y", mainStage.getY());
                        try{
                            iniWrite.store(new FileOutputStream("dat/settings.ini"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


    }
    public static Stage getStage(){return mainStage;}

    public static void main(String[] args) {
        launch();
        logger.info("Example log from {}", MainApplication.class.getSimpleName());
    }
}
