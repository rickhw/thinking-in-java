package com.gtcafe.asimov.platform.stock.rest;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.platform.stock.StockContext;
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
	public int operate(@RequestHeader(name = "X-Request-Id") String requestId, @RequestHeader(name = "X-JMeter-Thread-Name") String jmThreadName) throws Exception {

		int consumedUnit = (int) ((Math.random() * 1000) + 1) % 200;	// max: 200 unit
		long processTime = (long) (Math.random() * 20000);				// max: 20s

		StockContext context = new StockContext();
		context.setRequestId(requestId);
		// context.setJmThreadHame(jmThreadName);
		context.setConsumed(consumedUnit);
		context.setProcessTime(processTime);

		service.acquire(context);

		return cu.getValue();
	}

	@GetMapping("/value")
	public int getValue() {
		log.info("stock unit is [{}", cu.getValue());

		return cu.getValue();
	}
}
