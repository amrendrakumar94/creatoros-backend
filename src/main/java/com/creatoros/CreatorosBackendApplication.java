package com.creatoros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CreatorosBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatorosBackendApplication.class, args);
    }
}
