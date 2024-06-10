package com.gtcafe.springbootlab.day00;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private final Environment env;

    @Autowired
    private StudentRepository repos;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    public Main(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("CommandLineRunner.run()");
        logger.info("JAVA_HOME: [{}]", env.getProperty("JAVA_HOME"));
        logger.info("APP_NAME: [{}]", env.getProperty("APP_NAME"));
        // logger.info("app.name: [{}]", env.getProperty("app.name"));

        // write entity
        for(int i=0; i<100; i++) {
            StudentHash student = new StudentHash("Eng"+i, "John Doe", Gender.MALE, 1, new Date());
            repos.save(student);

        }

        // read all
        System.out.println(repos.findAll());

        // retrieve by Id
        try {
            // retrieve
            StudentHash retrievedStudent = repos.findById("Eng2015").get();
            System.out.println(retrievedStudent);

            // update by Id
            retrievedStudent.setName("Richard Watson");
            repos.save(retrievedStudent);
            System.out.println(retrievedStudent);
        } catch (java.util.NoSuchElementException ex) {
            System.out.println(ex.getMessage());
        }

    }

    @PreDestroy
    public void onExit() {
        logger.info("*** Application was stopped now ***");
    }

}
