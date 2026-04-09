package com.capgemini.homeloanmanagementapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class HomeLoanManagementAppApplication {
    public static void main(String[] args) {
        log.info("Starting Home Loan Management App...");
        SpringApplication.run(HomeLoanManagementAppApplication.class, args);
        log.info("Home Loan Management App started successfully.");
    }
}