package com.proclub.datareader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
//@EnableAdminServer
public class DatareaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatareaderApplication.class, args);
    }

}

