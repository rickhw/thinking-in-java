package com.gtcafe.springbootlab.day00;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static int EXIT_TIME = 1; // second

    public static void main(String[] args) {
        logger.info("Main.main()");
        logger.info("*** Application is starting now ***");
        SpringApplication.run(Main.class, args);
    }

    @PreDestroy
    public void onExit() {
        logger.info("Main@PreDestroy()");
        logger.info("*** Application is stopping now ***");
        try {
            Thread.sleep(EXIT_TIME * 1000);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        logger.info("*** Application was stopped now ***");
    }

    @PostConstruct
	public void populateMovieCache() {
        logger.info("Main@PostConstruct()");
        logger.info("*** Application is stopping now ***");
        try {
            Thread.sleep(EXIT_TIME * 1000);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        logger.info("*** Application was stopped now ***");
	}

}
