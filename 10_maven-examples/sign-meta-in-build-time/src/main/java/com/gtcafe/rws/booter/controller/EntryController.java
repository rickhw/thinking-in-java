package com.gtcafe.rws.booter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.rws.booter.HttpHeaderConstants;
import com.gtcafe.rws.booter.config.Utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@RestController
public class EntryController {

    Logger logger = LoggerFactory.getLogger(EntryController.class);

    @Autowired
    private Utils utils;

    @GetMapping(value = "/", produces = { "text/plain" })
    public ResponseEntity<String> getRootMessage(
            HttpServletRequest request, HttpServletResponse response) {

        String reqId = response.getHeader(HttpHeaderConstants.R_REQUEST_ID);
        if (reqId == null || "".equals(reqId)) {
            reqId = UUID.randomUUID().toString();
        }
        // logger.info("Entry Controller");

        return new ResponseEntity<>(utils.apiSlogan(reqId), HttpStatus.OK);
    }


}
