package com.proclub.datareader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
//@EnableAdminServer
public class DataReaderApplication {

    private static Logger _logger = LoggerFactory.getLogger(DataReaderApplication.class);

    public static void main(String[] args) {
        _logger.info("Starting...");

        for (String arg : args) {
            _logger.info(String.format(">>>>>  arg: %s", arg));
        }
        SpringApplication.run(DataReaderApplication.class, args);
    }

}

