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

import com.gtcafe.asimov.region.domain.ConcurrentOperationException;
import com.gtcafe.asimov.region.domain.RegionMapper;
import com.gtcafe.asimov.region.domain.RegionService;
import com.gtcafe.asimov.region.rest.request.CreateRegionRequest;
import com.gtcafe.asimov.region.rest.response.RetrieveRegionResponse;
import com.gtcafe.asimov.region.schema.Region;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1alpha")
@Tag(name = "Platform/Region", description = "Region APIs")
public class RegionController {

  @Autowired
  private RegionService service;

  @GetMapping(value = "/regions", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Query", description = "")
  public ResponseEntity<String> query() {
    return ResponseEntity.ok("ok");
  }

  @GetMapping(value = "/regions:flush", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Flush the data in cache", description = "")
  public ResponseEntity<String> flush() {
     try {
      service.flush();
        } catch (ConcurrentOperationException e) {
            // 處理併發情況
        } catch (Exception e) {
            // 處理其他錯誤
        }
    return ResponseEntity.ok("ok");
  }


  @PostMapping(value = "/regions", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Create", description = "")
  public ResponseEntity<RetrieveRegionResponse> create(
      @RequestBody
			// @Validated
			CreateRegionRequest request) {
        Region region = RegionMapper.mapRequestToDomain(request);

        service.create(region);
      
    return ResponseEntity.ok(RegionMapper.mapDomainToResponse(region));
  }


  @GetMapping(value = "/regions/{code}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Retrieve ", description = "")
  public ResponseEntity<RetrieveRegionResponse> retrieve(
    @Parameter(name ="code", description = "region code", required = true) @PathVariable("code") String regionCode) {

    Region region = service.retrieve(regionCode);
    
    return ResponseEntity.ok(RegionMapper.mapDomainToResponse(region));
  }

  @DeleteMapping(value = "/regions/{code}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Schema(name = "Delete ", description = "")
  public ResponseEntity<String> delete(
    @Parameter(name ="code", description = "region code", required = true) @PathVariable("code") String regionCode) {

    return ResponseEntity.ok(String.format("sent, eventId: [%s], message: [%s]"));
  }

}
