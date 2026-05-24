package com.minhthien.hoser_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HoserBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoserBackendApplication.class, args);
    }

}
