package com.example.retrydemo.controller;

import com.example.retrydemo.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping("/run-subprocesses")
    public String runSubprocesses() {
        boolean success = mainService.executeAllSubprocesses();
        return success ? "All subprocesses executed successfully." : "One or more subprocesses failed.";
    }
}
