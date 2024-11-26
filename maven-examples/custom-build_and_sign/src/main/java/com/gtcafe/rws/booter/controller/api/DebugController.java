package com.gtcafe.rws.booter.controller.api;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Debug API", description = "")
@RestController()
@RequestMapping("/api/debug")
public class DebugController {

  private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

  @GetMapping("/")
  public ResponseEntity<Map<String, Object>> root(
      @RequestHeader HttpHeaders headers,
      HttpServletRequest request,
      HttpServletResponse response) {

    Map<String, Object> payload = requestMap(headers, request, response);

    return new ResponseEntity<>(payload, HttpStatus.OK);
  }


  @GetMapping("/http206")
  public ResponseEntity<String> evaluate206() {
    return new ResponseEntity<String>("HTTP_206: Partial content", HttpStatus.PARTIAL_CONTENT);
  }

  @GetMapping("/http302")
  public ResponseEntity<String> evaluate302() {
    return new ResponseEntity<String>("HTTP_302: Found", HttpStatus.FOUND);
  }


  @GetMapping("/http400")
  public ResponseEntity<String> evaluate400BadRequest() {
    return new ResponseEntity<String>("HTTP_400: Bad Request", HttpStatus.BAD_REQUEST);
  }

  @GetMapping("/http403")
  public ResponseEntity<String> evaluate403Forbidden() {
    return new ResponseEntity<String>("HTTP_403: Forbidden", HttpStatus.FORBIDDEN);
  }

  @GetMapping("/http404")
  public ResponseEntity<String> evaluate404() {
    return new ResponseEntity<String>("HTTP_404: Not found", HttpStatus.NOT_FOUND);
  }

  @GetMapping("/http413")
  public ResponseEntity<String> evaluate413() {
    return new ResponseEntity<String>("HTTP_413: Payload too large", HttpStatus.PAYLOAD_TOO_LARGE);
  }

  @GetMapping("/http414")
  public ResponseEntity<String> evaluate414() {
    return new ResponseEntity<String>("HTTP_414: URI too long", HttpStatus.URI_TOO_LONG);
  }

  @GetMapping("/http418")
  public ResponseEntity<String> evaluated418() {
    return new ResponseEntity<String>("HTTP_418: I am a teapot", HttpStatus.I_AM_A_TEAPOT);
  }


  @GetMapping("/http500")
  public ResponseEntity<String> evaluate500InternalServerError() {
    return new ResponseEntity<String>("HTTP_500: Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @GetMapping("/http502")
  public ResponseEntity<String> evaluate502BadGateway() {
    return new ResponseEntity<String>("HTTP_502: Bad Gateway", HttpStatus.BAD_GATEWAY);
  }

  @GetMapping("/http504")
  public ResponseEntity<String> evaluate504() {
    return new ResponseEntity<String>("HTTP_504: Gateway timeout", HttpStatus.GATEWAY_TIMEOUT);
  }

  @GetMapping("/http509")
  public ResponseEntity<String> evaluate509BandwidthLimitExceeded() {
    return new ResponseEntity<String>("HTTP_509: Bandwidth limit exceeded", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
  }

  @GetMapping("/http508")
  public ResponseEntity<String> evaluate508() {
    return new ResponseEntity<String>("HTTP_508: Loop detected", HttpStatus.LOOP_DETECTED);
  }

  @GetMapping("/http501")
  public ResponseEntity<String> evaluate501() {
    return new ResponseEntity<String>("HTTP_501: Not implemented", HttpStatus.NOT_IMPLEMENTED);
  }

  // ----
  private Map<String, Object> requestMap(HttpHeaders headers,
      HttpServletRequest request,
      HttpServletResponse response) {

    // 1.
    // logger.info("listHeaders:");
    // headers.forEach((key, value) -> {
    //   logger.info("- [{}]: [{}]", key, value);
    // });

    Map<String, Object> responseHeaders = new TreeMap<>();
    for (String name : response.getHeaderNames()) {
      responseHeaders.put(name, response.getHeader(name));
    }


    // 2.
    Map<String, Object> requestPayload = new TreeMap<>();
    InetSocketAddress host = headers.getHost();

    requestPayload.put("host.hostname", host.getHostName());
    requestPayload.put("host.port", host.getPort());

    requestPayload.put("request.scheme", request.getScheme());
    requestPayload.put("request.queryString", request.getQueryString());

    requestPayload.put("request.requestId", request.getRequestId());
    requestPayload.put("request.requestURI", request.getRequestURI());
    requestPayload.put("request.requestURL", request.getRequestURL());

    requestPayload.put("request.authType", request.getAuthType());
    requestPayload.put("request.characterEncoding", request.getCharacterEncoding());
    requestPayload.put("request.contentType", request.getContentType());
    requestPayload.put("request.contextPath", request.getContextPath());
    requestPayload.put("request.method", request.getMethod());
    requestPayload.put("request.pathInfo", request.getPathInfo());
    requestPayload.put("request.cookies", request.getCookies());
    requestPayload.put("request.protocol", request.getProtocol());

    requestPayload.put("request.localAddr", request.getLocalAddr());
    requestPayload.put("request.localName", request.getLocalName());
    requestPayload.put("request.localPort", request.getLocalPort());

    requestPayload.put("request.remoteAddr", request.getRemoteAddr());
    requestPayload.put("request.remoteHost", request.getRemoteHost());
    requestPayload.put("request.remotePort", request.getRemotePort());
    requestPayload.put("request.remoteUser", request.getRemoteUser());

    requestPayload.put("request.serverName", request.getServerName());
    requestPayload.put("request.serverPort", request.getServerPort());

    requestPayload.put("request.userPrincipal", request.getUserPrincipal());

    // logger.info(requestPayload.toString());

    // 2. Response


    // 3. Final
    Map<String, Object> payload = new TreeMap<>();
    payload.put("headers", headers);
    payload.put("response.headers", responseHeaders);
    payload.put("requests", requestPayload);

    return payload;
  }
}
