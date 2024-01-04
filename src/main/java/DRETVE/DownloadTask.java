package DRETVE;

import com.example.menze.MeniViewController;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import javafx.concurrent.Task;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.IOUtils;
import org.asynchttpclient.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadTask extends Task<Void> {
    private String url, saveAtUrl;
    private Integer speed;

    private final MeniViewController controller;

    public DownloadTask(String url, String saveAtUrl, Integer speed, MeniViewController controller){
        this.url = url;
        this.saveAtUrl = saveAtUrl;
        this.speed = speed;
        this.controller = controller;
    }

    @Override
    protected Void call() throws Exception {
        URLConnection connection = new URL(url).openConnection();
        long fileLength = connection.getContentLengthLong();

        try(InputStream is = connection.getInputStream();
            FileOutputStream os = new FileOutputStream(saveAtUrl))
        {
            long startTime = System.currentTimeMillis();
            long nread = 0L;
            byte[] buf = new byte[8192];
            int n;
            while((n = is.read(buf, 0 , 8192))>0){
                os.write(buf, 0, n);
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                long sleepTime = (n * 1000) / speed - elapsedTime;

                nread = (n* 100L)/fileLength;
                controller.updateProgressBar(nread);
                if(sleepTime > 0){
                    Thread.sleep(sleepTime);
                }
                startTime = System.currentTimeMillis();

            }
        }
        return null;
    }

    @Override
    protected void succeeded() {
        System.out.println("Downloaded");
    }

    @Override
    protected void failed() {
        System.out.println("Failed download");
    }


}
