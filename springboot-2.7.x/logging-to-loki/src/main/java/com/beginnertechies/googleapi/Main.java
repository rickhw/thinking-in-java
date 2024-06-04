package com.gtcafe.race;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.gtcafe.race.bean.*;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
    public ICapacityUnit capacityUnit() {
        return new ReentrantCapacityUnit();
		// return new NormalCapacityUnit();
    }


}
