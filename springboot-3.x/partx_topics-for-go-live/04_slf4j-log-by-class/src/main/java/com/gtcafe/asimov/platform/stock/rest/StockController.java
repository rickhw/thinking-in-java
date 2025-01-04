package com.gtcafe.asimov.platform.stock.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.stock.StockService;
import com.gtcafe.asimov.platform.stock.domain.ReentrantStockCounter;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1alpha/stock")
@Slf4j
@Tag(name = "Platform/Stock", description = "Stock API")
public class StockController {

	@Autowired
	private StockService service;

	@Autowired
	private ReentrantStockCounter cu;

	@GetMapping("/consume")
	public int operate() throws Exception {
		// random unit value, between 1 and 100
		int unit = (int) ((Math.random() * 1000) + 1) % 10;

		service.acquire(unit);

		return cu.getValue();
	}

	@GetMapping("/value")
	public int getValue() {
		log.info("stock unit is [{}", cu.getValue());

		return cu.getValue();
	}
}
