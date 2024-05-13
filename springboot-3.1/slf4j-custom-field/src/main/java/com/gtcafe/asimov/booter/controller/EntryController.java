package com.gtcafe.asimov.booter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class EntryController {

    Logger logger = LoggerFactory.getLogger(EntryController.class);

    @GetMapping(value = "/", produces = { "text/plain" })
    public ResponseEntity<String> getRootMessage(
            HttpServletRequest request, HttpServletResponse response) {
            
        logger.info("Entry Controller");

        // logger.

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


}
