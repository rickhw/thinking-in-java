package com.gtcafe.springbootlab.day00;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class BootService implements CommandLineRunner, ApplicationRunner  {

    private static final Logger logger = LoggerFactory.getLogger(BootService.class);

    private final Environment env;

	public BootService(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("CommandLineRunner.run(): Application Starting ...");
        logger.info("JAVA_HOME: [{}]", env.getProperty("JAVA_HOME"));
        logger.info("APP_NAME: [{}]", env.getProperty("APP_NAME"));
        logger.info("app.name: [{}]", env.getProperty("app.name"));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("ApplicationRunner.run(): Application Starting ...");
        // 1. show environment variables of application

        // 2. show config path from ...

        // 3. show slogan, and tell user the application is ready.
    }

}