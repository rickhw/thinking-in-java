package com.beginnertechies.googleapi.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import com.beginnertechies.googleapi.bean.*;

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

}
