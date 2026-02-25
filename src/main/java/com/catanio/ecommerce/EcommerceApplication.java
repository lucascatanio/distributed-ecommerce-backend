package com.catanio.ecommerce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EcommerceApplication {

    private static final Logger log = LoggerFactory.getLogger(EcommerceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationReady() {
        log.info("E-Commerce Monolith started successfully. Sprint 1 — Domain Model ready.");
    }
}
