module com.example.menze {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires ini4j;
    requires json.simple;
    requires java.xml.bind;
    requires itextpdf;
    requires pdfbox;
    requires java.desktop;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires jakarta.ws.rs;
    requires async.http.client.netty.utils;
    requires async.http.client;


    opens com.example.menze to javafx.fxml;
    exports com.example.menze;
}