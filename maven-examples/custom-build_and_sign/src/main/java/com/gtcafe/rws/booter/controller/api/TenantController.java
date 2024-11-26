package com.gtcafe.rws.booter.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.rws.booter.entity.AccountEntity;
import com.gtcafe.rws.booter.entity.TenantEntity;
import com.gtcafe.rws.booter.exception.ResourceNotFoundException;
import com.gtcafe.rws.booter.payload.request.CreateTenantRequest;
import com.gtcafe.rws.booter.payload.response.RetrieveTenantResponse;
import com.gtcafe.rws.booter.payload.standard.response.StandardResponse;
import com.gtcafe.rws.booter.repository.AccountRepository;
import com.gtcafe.rws.booter.repository.TenantRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// @CrossOrigin(origins = {"http://localhost:3000", "https://dapr.lab.gtcafe.com"})
// @OpenAPIDefinition(info = @Info(title = "Ruyut Example", version = "1.0.0"))
@Tag(name = "Tenant", description = "租戶資訊")
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

	Logger logger = LoggerFactory.getLogger(TenantController.class);

	@Autowired
	private TenantRepository repos;

	@Autowired
	private AccountRepository accountRepos;

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Retrieve all tenants with condition",
		description = "Retrieve all tenants with condition"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = CreateTenantRequest.class), mediaType = "application/json")
			}
		)
	})
	@GetMapping()
	public List<TenantEntity> retrievTenantEntities() {
		return repos.findAll();
	}


	// ------------------------------------------------------------------------
	@Operation(
		summary = "Retrieve all tenants with condition",
		description = "Retrieve all tenants with condition"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = CreateTenantRequest.class), mediaType = "application/json")
			}
		)
	})
	@PostMapping()
	public ResponseEntity<StandardResponse> create(@RequestBody CreateTenantRequest request) {

		TenantEntity entity = new TenantEntity(request.getTenantName(), request.getDescription());

		// TODO
		// get AccountEntity
		AccountEntity account =  accountRepos
			.findById(Long.valueOf(request.getRootAccountId()))
			.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + request.getRootAccountId()));
			// .get();


		entity.setRootAccount(account);
		repos.save(entity);

		StandardResponse res = new StandardResponse("OK", "Success");

		return ResponseEntity.ok(res);
	}

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Retrieve the tenants with condition",
		description = "Retrieve the tenants with condition"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = RetrieveTenantResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The Tenant with given Id was not found.",
			content = { @Content(schema = @Schema()) }
		)
	})
	@GetMapping("/{id}")
	public ResponseEntity<RetrieveTenantResponse> retrieveById(@PathVariable Long id) {

		TenantEntity entity = repos.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + id));


		RetrieveTenantResponse response = new RetrieveTenantResponse();
		response.setId(entity.getId());
		response.setTenantName(entity.getTenantName());
		response.setDescription(entity.getDescription());
		response.setRootAccountId(entity.getRootAccount().getId());

		return ResponseEntity.ok(response);


	}


	// ------------------------------------------------------------------------
	@PatchMapping("/{id}")
	public ResponseEntity<TenantEntity> updateMember(@PathVariable Long id, @RequestBody TenantEntity entityDetails) {
		TenantEntity entity = repos.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + id));

		entity.setTenantName(entityDetails.getTenantName());
		entity.setDescription(entityDetails.getDescription());

		TenantEntity updatedEntity = repos.save(entity);

		return ResponseEntity.ok(updatedEntity);
	}

	// ------------------------------------------------------------------------
	@PatchMapping("/{id}:change-root-account")
	public ResponseEntity<TenantEntity> changeRootAccount(@PathVariable Long id, @RequestBody TenantEntity entityDetails) {
		TenantEntity entity = repos.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + id));

		entity.setTenantName(entityDetails.getTenantName());
		entity.setDescription(entityDetails.getDescription());

		TenantEntity updatedEntity = repos.save(entity);

		return ResponseEntity.ok(updatedEntity);
	}

		// ------------------------------------------------------------------------
	@GetMapping("/{id}:get-root-account")
	public ResponseEntity<AccountEntity> getRootAccount(@PathVariable Long id) {
		TenantEntity entity = repos.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + id));

		return ResponseEntity.ok(entity.getRootAccount());
	}


	// ------------------------------------------------------------------------
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteMember(@PathVariable Long id) {

		TenantEntity member = repos.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not exist with id :" + id));

		repos.delete(member);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);

		return ResponseEntity.ok(response);
	}

}
