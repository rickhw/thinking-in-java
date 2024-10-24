package com.gtcafe.race.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;

import com.gtcafe.race.bean.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class RootController {

	@Autowired
	private ICapacityUnit cu;

	@GetMapping("/operate")
	public int readDataFromGoogleSheet(@RequestParam(required = true) Integer value) throws GeneralSecurityException, IOException {

		cu.operate(value);

		return cu.getValue();
	}


	@GetMapping("/value")
	public int getValue() {

		return cu.getValue();
	}


	@GetMapping("/reset")
	public int reset() {

		cu.reset();

		return cu.getValue();
	}

	@GetMapping("/file")
	public int file(@RequestParam(required = true) Integer value) {

		System.out.println(new Date());
		try {
			Thread.sleep(value);
		} catch (Exception e) {}

		System.out.println(new Date());

		return cu.getValue();
	}

	@GetMapping("/metric")
	// public String file(@RequestParam(required = true) String metricName) {
	public String metric() {

		String metric = String.format("%s,%d,%d", new Date(), (int)(Math.random() * 100), (int)(Math.random() * 10000));

		return metric;
	}

}
