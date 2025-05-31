package ru.artemaweirro.rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LibraryApp {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LibraryApp.class);
        app.run(args);
    }
}
