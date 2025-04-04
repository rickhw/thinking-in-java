package com.gtcafe.asimov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gtcafe.asimov.platform.stock.counter.ICapacityUnit;
import com.gtcafe.asimov.platform.stock.counter.ReentrantCapacityUnit;

@SpringBootApplication
@EnableScheduling
public class Main { 
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@Bean
    public ICapacityUnit capacityUnit() {
		// return new NoconstraintCapacityUnit();
		// return new NolockCapacityUnit();
        return new ReentrantCapacityUnit();
    }
}
