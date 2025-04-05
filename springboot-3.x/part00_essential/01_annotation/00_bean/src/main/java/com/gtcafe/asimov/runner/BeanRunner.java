package com.gtcafe.asimov.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.gtcafe.asimov.bean.CustomBean;

@Component
public class BeanRunner implements CommandLineRunner {

    private final CustomBean customBean;

    public BeanRunner(CustomBean customBean) {
        this.customBean = customBean;
    }

    @Override
    public void run(String... args) {
        System.out.println(customBean.getMessage());
    }
}