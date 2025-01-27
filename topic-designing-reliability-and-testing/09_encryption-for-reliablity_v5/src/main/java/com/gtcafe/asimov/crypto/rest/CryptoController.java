package com.gtcafe.asimov.crypto.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.crypto.CryptoConstants;
import com.gtcafe.asimov.crypto.capacity.ICapacityUnit;
import com.gtcafe.asimov.crypto.domain.CryptoContext;
import com.gtcafe.asimov.crypto.domain.CryptoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/crypto")
@Slf4j
@Tag(name = "Crypto", description = "Crypto API")
public class CryptoController {

	@Autowired
	private CryptoService service;

	@Autowired
	private ICapacityUnit capacityUnit;

	@GetMapping("/consume")
	public int operate(
		@RequestHeader(name = CryptoConstants.X__REQUEST_ID) String requestId, 
		@RequestHeader(name = "X-JMeter-Thread-Name") String jmThreadName
	) throws Exception {

		int consumedUnit = (int) ((Math.random() * 1000) + 1) % 14;	// max: 200 unit
		long processTime = (long) (Math.random() * 20000);				// max: 20s

		CryptoContext context = new CryptoContext();
		context.setRequestId(requestId);
		// context.setJmThreadHame(jmThreadName);
		context.setConsumed(consumedUnit);
		context.setProcessTime(processTime);

		service.acquire(context);

		return capacityUnit.remaining();
	}

	@GetMapping("/remaining")
	public int getValue() {
		log.info("remaining is [{}", capacityUnit.remaining());

		return capacityUnit.remaining();
	}
}
