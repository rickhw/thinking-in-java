package com.gtcafe.springbootlab.day01;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        System.out.println("Max file size: " + environment.getProperty("spring.servlet.multipart.max-file-size"));
        System.out.println("email.smtp.server: " + environment.getProperty("email.smtp.server"));

    }
}
