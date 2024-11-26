package com.gtcafe.rws.booter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main { //implements ApplicationRunner  {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}


//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//         // 1. show environment variables of application

//         // 2. show config path from ...

//         // 3. show slogan, and tell user the application is ready.
//        System.out.println(utils.slogan());
//        System.out.println(String.format("Datasource: [%s]", dataSource));
//    }
}
