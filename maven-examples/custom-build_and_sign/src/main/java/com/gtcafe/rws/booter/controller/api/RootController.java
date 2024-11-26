package com.gtcafe.rws.booter.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.rws.booter.HttpHeaderConstants;
import com.gtcafe.rws.booter.config.Releng;
import com.gtcafe.rws.booter.config.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "API Metadata", description = "")
@RestController
@RequestMapping("/api")
public class RootController {

    Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    private Utils utils;

    @Autowired
    private Releng releng;

    @Operation(
      summary = "API for slogan",
      description = "API for slogan"
    )
    @ApiResponses({
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(schema = @Schema(), mediaType = "text/plain"),
          @Content(schema = @Schema(), mediaType = "application/json"),
        }
      )
    })
    @GetMapping(value = "/", produces = { "application/json", "text/plain" })
    public ResponseEntity<?> getRootMessage(
            HttpServletRequest request, HttpServletResponse response) {

        final String acceptHeader = request.getHeader("Accept");
        String reqId = response.getHeader(HttpHeaderConstants.R_REQUEST_ID);

        logger.info("HelloAPI invoked");

        if (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return new ResponseEntity<>(releng, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(utils.apiSlogan(reqId), HttpStatus.OK);
        }
    }

    @Operation(
      summary = "Artifact Build Metadata",
      description = "Artifact Build Metadata"
    )
    @ApiResponses({
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(schema = @Schema(), mediaType = "application/json")
        }
      )
    })
    @GetMapping("/releng")
    public ResponseEntity<?> releng() {

        logger.info(releng.toString());

        return new ResponseEntity<>(releng, HttpStatus.OK);
    }

    @Operation(
      summary = "Release Notes",
      description = "Release Notes"
    )
    @ApiResponses({
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(schema = @Schema(), mediaType = "application/json")
        }
      )
    })
    @GetMapping(value = "/release-notes", produces = { "text/plain"} )
    public ResponseEntity<?> releaseNotes() {

        // logger.info(releng.toString());

        return new ResponseEntity<>(utils.releaseNotes(), HttpStatus.OK);
    }

  @Operation(
    summary = "A public API, for test,",
    description = "not need to authorized"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(schema = @Schema(), mediaType = "application/json")
      }
    )
  })
  @GetMapping("/public")
  public ResponseEntity<String> publicAction() {

    logger.info("invoke /public");

    // 5. response message
    return ResponseEntity.ok("Public API does not require authorization.");
  }

  @Operation(
    summary = "A protected API, for test,",
    description = "need to authorized"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(schema = @Schema(), mediaType = "application/json")
      }
    )
  })
  @GetMapping("/whoami")
  public ResponseEntity<String> protectedWhoami(HttpServletRequest request, HttpServletResponse response) {

    // String authToken = request.getHeader(HttpHeaderConstants.R_AUTH_TOKEN);
    // logger.info("before: R-Auth-Token: [{}]", authToken);
    logger.info("invoke /whoami, AuthToken: [{}], authToken");

    // 5. response message
    return ResponseEntity.ok("The whoami API needs to authorization");
  }


  @Operation(
    summary = "A private API to retrieve config information for debug",
    description = "to retrieve config information, need to authorized"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(schema = @Schema(), mediaType = "application/json")
      }
    )
  })
  @GetMapping("/private/config")
  public ResponseEntity<String> privateRetrieveConfig(HttpServletRequest request, HttpServletResponse response) {

    logger.info("invoke /private/config, AuthToken: [{}], authToken");

    // 5. response message
    return ResponseEntity.ok("retrieve config, database status, internal depends ...");
  }

  @Operation(
    summary = "A private API to retrieve config information for debug",
    description = "to retrieve config information, need to authorized"
  )
  @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      content = {
        @Content(schema = @Schema(), mediaType = "application/json")
      }
    )
  })
  @GetMapping("/private/healthy")
  public ResponseEntity<String> privateHealthy(HttpServletRequest request, HttpServletResponse response) {

    logger.info("invoke /private/healthy, AuthToken: [{}], authToken");

    // 5. response message
    return ResponseEntity.ok("retrieve config, database status, internal depends ...");
  }

}
