package com.gtcafe.asimov;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiClassMeta(description = "Demo controller for API testing", tenantAware = true, kind = "endpoint")
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/sync-endpoint")
    public String syncEndpoint() {
        return "Sync endpoint";
    }

    @ApiMethodMeta(execMode = "async")
    @GetMapping("/async-endpoint")
    public String asyncEndpoint() {
        return "Async endpoint";
    }
}