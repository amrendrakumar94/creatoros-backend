package com.creatoros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { HibernateJpaAutoConfiguration.class, DataJpaRepositoriesAutoConfiguration.class })
@ConfigurationPropertiesScan
@EnableScheduling
public class CreatorosBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreatorosBackendApplication.class, args);
    }
}
