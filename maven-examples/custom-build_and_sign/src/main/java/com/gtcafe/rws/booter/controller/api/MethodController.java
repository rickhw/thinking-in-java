package com.gtcafe.rws.booter.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "HTTP Methods", description = "")
@RestController()
@RequestMapping("/api/methods")
public class MethodController {

  private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

  // ----
  @GetMapping("/{pathParam}")
  public ResponseEntity<String> getMethod(
    @RequestParam(required = false, defaultValue = "false") String disabled,
    @PathVariable String pathParam
  ) {
    StringBuffer sb = new StringBuffer();

    sb.append(String.format("Method: HTTP GET\n"));
    sb.append(String.format("disabled: [%s]\n", disabled));
    sb.append(String.format("pathParam: [%s]\n", pathParam));

    return new ResponseEntity<String>(sb.toString(), HttpStatus.ACCEPTED);
  }

  @PostMapping()
  public ResponseEntity<String> postMethod() {
    return new ResponseEntity<String>("Method: HTTP POST", HttpStatus.ACCEPTED);
  }

  @DeleteMapping()
  public ResponseEntity<String> deleteMethod() {
    return new ResponseEntity<String>("Method: HTTP DELETE", HttpStatus.ACCEPTED);
  }

  @PatchMapping()
  public ResponseEntity<String> patchMethod() {
    return new ResponseEntity<String>("Method: HTTP PATCH", HttpStatus.ACCEPTED);
  }

  @PutMapping()
  public ResponseEntity<String> putMethod() {
    return new ResponseEntity<String>("Method: HTTP PUT", HttpStatus.ACCEPTED);
  }

  @ConnectMapping()
  // @RequestMapping( method = RequestMethod.CONNECT)
  public ResponseEntity<String> connectMethod() {
    return new ResponseEntity<String>("Method: HTTP CONNECT", HttpStatus.ACCEPTED);
  }

  @RequestMapping( method = { RequestMethod.HEAD })
  public ResponseEntity<String> headMethod() {
    System.out.println("head");
    return new ResponseEntity<String>("Method: HTTP HEAD", HttpStatus.ACCEPTED);
  }

  @RequestMapping(method = { RequestMethod.OPTIONS })
  public ResponseEntity<String> optionsMethod() {
    return new ResponseEntity<String>("Method: HTTP OPTIONS", HttpStatus.ACCEPTED);
  }

  @RequestMapping(method = { RequestMethod.TRACE })
  public ResponseEntity<String> traceMethod() {
    return new ResponseEntity<String>("Method: HTTP TRACE", HttpStatus.ACCEPTED);
  }

}
