package com.example.backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class BackendApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("BackendApplication started on port 8081");
    }
}