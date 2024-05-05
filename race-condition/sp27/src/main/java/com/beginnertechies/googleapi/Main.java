package com.beginnertechies.googleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.beginnertechies.googleapi.bean.CapacityUnit;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
    public CapacityUnit capacityUnit() {
        return new CapacityUnit();
    }


}
