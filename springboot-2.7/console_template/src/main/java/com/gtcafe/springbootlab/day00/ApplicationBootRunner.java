package com.gtcafe.springbootlab.day00;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class ApplicationBootRunner implements ApplicationRunner  {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationBootRunner.class);


    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("ApplicationRunner.run(): Application Starting ...");
        // 1. show environment variables of application

        // 2. show config path from ...

        // 3. show slogan, and tell user the application is ready.
    }

}
