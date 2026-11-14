package com.alumnisearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class AlumniLinkedinSearcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlumniLinkedinSearcherApplication.class, args);
    }
}
