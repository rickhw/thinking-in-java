package com.gtcafe.asimov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main implements ApplicationRunner {

	// @Autowired
	// private EmployeeService employeeService;

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // employeeService.update();

    }
}
