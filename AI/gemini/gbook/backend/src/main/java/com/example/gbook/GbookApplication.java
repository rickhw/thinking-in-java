package com.example.gbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(GbookApplication.class, args);
	}

}
