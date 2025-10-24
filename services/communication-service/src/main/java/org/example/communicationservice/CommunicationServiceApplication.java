package org.example.communicationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CommunicationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommunicationServiceApplication.class, args);
  }

}
