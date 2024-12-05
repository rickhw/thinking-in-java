package com.gtcafe.asimov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @PostMapping
    public String logEvent(@RequestBody Map<String, String> payload) {
        logger.info("Received log event: {}", payload);
        return "Log recorded";
    }
}
