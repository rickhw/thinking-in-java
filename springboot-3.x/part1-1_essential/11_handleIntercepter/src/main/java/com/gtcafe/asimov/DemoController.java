package com.gtcafe.asimov;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ControllerInfo(description = "Demo controller for API testing", tenantAware = true)
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/sync-endpoint")
    public String syncEndpoint() {
        return "Sync endpoint";
    }

    @ApiMeta(execMode = "async", kind = "endpoint")
    @GetMapping("/async-endpoint")
    public String asyncEndpoint() {
        return "Async endpoint";
    }
}