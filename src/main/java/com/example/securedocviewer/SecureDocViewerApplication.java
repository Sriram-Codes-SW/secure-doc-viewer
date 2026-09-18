package com.example.securedocviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureDocViewerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureDocViewerApplication.class, args);
    }
}
