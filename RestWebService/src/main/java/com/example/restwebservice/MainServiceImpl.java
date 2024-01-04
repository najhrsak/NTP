package com.example.restwebservice;

import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
public class MainServiceImpl implements MainService{
    @Override
    public String getDanas() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");
        return LocalDateTime.now().format(dtf);
    }

    @Override
    public String getRV(){
        return "Pon-Pet -> 00:00-00:00\nSub -> 00:00-00:00\nNed -> 00:00-00:00";
    }


    @Override
    public Resource getJelovnik(String file) throws IOException {
        return new UrlResource(Path.of("dat/"+ file).toUri());
    }
}
