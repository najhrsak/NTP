package com.example.restwebservice;

import jdk.jfr.ContentType;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("rest")
@AllArgsConstructor
public class MainController {
    private MainService mainService;

    @GetMapping("danas")
    public String getDanas(){
        return mainService.getDanas();
    }

    @GetMapping("radno-vrijeme")
    public String getRV(){
        return mainService.getRV();
    }

    @GetMapping(value = "jelovnik/{file}")
    public ResponseEntity<?> getJelovnik(@PathVariable("file") String file) {
        Resource resource = null;
        try{
            resource = mainService.getJelovnik(file);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if(resource == null){
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }

    //,    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
}
