package com.gtcafe.asimov.region.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.asimov.region.RegionMapper;
import com.gtcafe.asimov.region.RegionService;
import com.gtcafe.asimov.region.rest.request.CreateRegionRequest;
import com.gtcafe.asimov.region.rest.response.RetrieveRegionResponse;
import com.gtcafe.asimov.region.schema.Region;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1alpha/regions")
@Tag(name = "Platform/Region", description = "Region APIs")
public class RegionController {

  @Autowired
  private RegionService service;

  @GetMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Query", description = "")
  public ResponseEntity<String> query() {
    return ResponseEntity.ok("ok");
  }


  @PostMapping(value = "", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Create", description = "")
  public ResponseEntity<RetrieveRegionResponse> create(
      @RequestBody
			// @Validated
			CreateRegionRequest request) {
        Region region = RegionMapper.mapRequestToDomain(request);

        service.create(region);
      
    return ResponseEntity.ok(RegionMapper.mapDomainToResponse(region));
  }

  @GetMapping(value = "/{region-code}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Retrieve ", description = "")
  public ResponseEntity<RetrieveRegionResponse> retrieve(
    @Parameter(name ="region-code", description = "regionCode", required = true) @PathVariable("region-code") String regionCode) {

    Region region = service.retrieve(regionCode);
    
    return ResponseEntity.ok(RegionMapper.mapDomainToResponse(region));
  }

  @DeleteMapping(value = "/{region-code}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Delete ", description = "")
  public ResponseEntity<String> delete(
    @Parameter(name ="region-code", description = "regionCode", required = true) @PathVariable String regionCode) {

    return ResponseEntity.ok(String.format("sent, eventId: [%s], message: [%s]"));
  }

}
