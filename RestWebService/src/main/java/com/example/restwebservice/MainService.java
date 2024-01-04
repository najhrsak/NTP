package com.example.restwebservice;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public interface MainService {
    String getDanas();
    String getRV();
    Resource getJelovnik(String file) throws IOException;
}
