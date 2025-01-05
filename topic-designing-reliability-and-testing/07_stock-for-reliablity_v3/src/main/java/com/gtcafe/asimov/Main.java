package com.gtcafe.asimov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gtcafe.asimov.platform.stock.counter.IStockCounter;
import com.gtcafe.asimov.platform.stock.counter.NoconstraintStockCounter;
import com.gtcafe.asimov.platform.stock.counter.NolockStockCounter;
import com.gtcafe.asimov.platform.stock.counter.ReentrantStockCounter;

@SpringBootApplication
@EnableScheduling
public class Main { 
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@Bean
    public IStockCounter stockCounter() {
		return new NoconstraintStockCounter();
        // return new ReentrantStockCounter();
		// return new NolockStockCounter();
    }
}
