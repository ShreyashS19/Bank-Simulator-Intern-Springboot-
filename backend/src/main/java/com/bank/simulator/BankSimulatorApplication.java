package com.bank.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BankSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankSimulatorApplication.class, args);
    }
}
