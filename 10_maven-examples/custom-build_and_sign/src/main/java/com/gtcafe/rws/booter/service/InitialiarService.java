package com.gtcafe.rws.booter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.gtcafe.rws.booter.config.Utils;

@Service
public class InitialiarService implements CommandLineRunner, ApplicationRunner  {

    private static final Logger logger = LoggerFactory.getLogger(InitialiarService.class);

	@Autowired
    private Utils utils;

    private final Environment env;

	public InitialiarService(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("CommandLineRunner.run()");
        logger.info("JAVA_HOME: [{}]", env.getProperty("JAVA_HOME"));
        logger.info("APP_NAME: [{}]", env.getProperty("APP_NAME"));
        logger.info("app.name: [{}]", env.getProperty("app.name"));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("ApplicationRunner.run()");

        System.out.println(utils.slogan());
    }

}
