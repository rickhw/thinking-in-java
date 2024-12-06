package com.gtcafe.asimov;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class AsyncController {

    @Autowired
    private AsyncService asyncService;

    @GetMapping("/async-process")
    public CompletableFuture<String> asyncProcess() {
        log.info("Controller: Received request");
        return asyncService.performAsyncTask()
                .thenApply(result -> {
                    log.info("Controller: Sending response");
                    return result;
                });
    }
}
