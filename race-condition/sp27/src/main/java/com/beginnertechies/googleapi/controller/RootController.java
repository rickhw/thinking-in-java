package com.beginnertechies.googleapi.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import com.beginnertechies.googleapi.bean.CapacityUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class RootController {

	@Autowired
	private CapacityUnit cu;
	
	@GetMapping("/operate")
	public int readDataFromGoogleSheet(@RequestParam(required = true) Integer value) throws GeneralSecurityException, IOException {

		cu.operate(value);

		return cu.getValue();
	}

}
