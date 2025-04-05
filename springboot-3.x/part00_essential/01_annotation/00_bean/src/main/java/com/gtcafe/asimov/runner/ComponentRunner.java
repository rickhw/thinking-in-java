package com.gtcafe.asimov.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gtcafe.asimov.BeanInspector;
import com.gtcafe.asimov.service.MyService;

@Component
public class ComponentRunner implements CommandLineRunner {

    private final MyService myService;
    private final BeanInspector beanInspector;

    public ComponentRunner(MyService myService, BeanInspector beanInspector) {
        this.myService = myService;
        this.beanInspector = beanInspector;
    }

    @Override
    public void run(String... args) {
        System.out.println(myService.getMessage());

        beanInspector.listBeans();
    }
}
