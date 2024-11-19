package com.gtcafe.springbootlab.day00;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class CliRunnerService implements CommandLineRunner  {

    private static final Logger logger = LoggerFactory.getLogger(CliRunnerService.class);

    private final Environment env;

	public CliRunnerService(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("CommandLineRunner.run(): Application Starting ...");
        logger.info("JAVA_HOME: [{}]", env.getProperty("JAVA_HOME"));
        logger.info("APP_NAME: [{}]", env.getProperty("APP_NAME"));
        logger.info("app.name: [{}]", env.getProperty("app.name"));
    }

}
