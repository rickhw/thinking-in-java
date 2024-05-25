package com.gtcafe.rws.booter.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import  com.gtcafe.rws.booter.config.Releng;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class EntryController {

    Logger logger = LoggerFactory.getLogger(EntryController.class);

    @Autowired
    Releng releng;

    @GetMapping(value = "/", produces = { "application/json" })
    public ResponseEntity<String> getRootMessage(
            HttpServletRequest request, HttpServletResponse response) {

        String reqId = UUID.randomUUID().toString();
        // logger.info("Entry Controller");

        return new ResponseEntity<>(releng.toString(), HttpStatus.OK);
    }


}
