package com.gtcafe.rws.booter.controller.api;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.rws.booter.entity.AccountEntity;
import com.gtcafe.rws.booter.payload.request.CreateAccountRequest;
import com.gtcafe.rws.booter.payload.response.RetrieveAccountResponse;
import com.gtcafe.rws.booter.payload.standard.response.StandardErrorResponse;
import com.gtcafe.rws.booter.payload.standard.response.StandardResponse;
import com.gtcafe.rws.booter.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// @CrossOrigin(origins = {"http://localhost:3000", "https://dapr.lab.gtcafe.com"})
// @OpenAPIDefinition(info = @Info(title = "Ruyut Example", version = "1.0.0"))
@Tag(name = "Account", description = "Root account for tenant, support local and SSO from Google")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountService service;

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Query all accounts with condition",
		description = "Query all accounts with condition"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = CreateAccountRequest.class), mediaType = "application/json")
			}
		)
	})
	@GetMapping(value = {""}, consumes = "application/json")
	public List<AccountEntity> retrieveAllEntities(
		@RequestParam(required = false, defaultValue = "false")
		String disabled
	) {

		return service.query(disabled);
	}

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Create a new account",
		description = "Create a new account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = StandardResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@PostMapping("")
	public ResponseEntity<StandardResponse> createEntity(
			@RequestBody
			@Validated
			CreateAccountRequest request) {

		StandardResponse res = service.create(request);

		return ResponseEntity.ok(res);
	}

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Retrieve the account",
		description = "Retrieve the account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = RetrieveAccountResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The account with given Id was not found.",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@GetMapping("/{id}")
	public ResponseEntity<RetrieveAccountResponse> retrieveEntityById(@PathVariable Long id) {

		RetrieveAccountResponse response = service.retrieveEntityById(id);

		return ResponseEntity.ok(response);
	}


	// ------------------------------------------------------------------------
	@Operation(
		summary = "Partial update the account",
		description = "Partial update the account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = StandardResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The account with given Id was not found.",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@PatchMapping("/{id}")
	public ResponseEntity<AccountEntity> updateEntity(@PathVariable Long id, @RequestBody AccountEntity entityDetails) {

		AccountEntity updatedEntity = service.updateEntity(id, entityDetails);

		return ResponseEntity.ok(updatedEntity);
	}


	// ------------------------------------------------------------------------
	@Operation(
		summary = "Delete the account",
		description = "Delete the account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = StandardResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The account with given Id was not found.",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteEntity(@PathVariable Long id) {

		Map<String, Boolean> response = service.delete(id);

		return ResponseEntity.ok(response);
	}


	// ------------------------------------------------------------------------
	@Operation(
		summary = "Disable the account",
		description = "Disable the account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = StandardResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The account with given Id was not found.",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@GetMapping("/{id}:disable")
	public ResponseEntity<AccountEntity> disableEntityById(@PathVariable Long id) {

		AccountEntity updatedEntity = service.disableEntityById(id);

		return ResponseEntity.ok(updatedEntity);
	}

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Enable the account",
		description = "Enable the account"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = StandardResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid Request",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		),
		@ApiResponse(
			responseCode = "404",
			description = "The account with given Id was not found.",
			content = {
				@Content(schema = @Schema(implementation = StandardErrorResponse.class), mediaType = "application/json")
			}
		)
	})
	@GetMapping("/{id}:enable")
	public ResponseEntity<AccountEntity> enableEntityById(@PathVariable Long id) {

		AccountEntity updatedEntity = service.enableEntityById(id);

		return ResponseEntity.ok(updatedEntity);
	}
}
