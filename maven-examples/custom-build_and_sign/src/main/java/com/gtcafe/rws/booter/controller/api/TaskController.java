package com.gtcafe.rws.booter.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.rws.booter.payload.request.CreateTaskRequest;
import com.gtcafe.rws.booter.payload.response.CreateTaskResponse;
import com.gtcafe.rws.booter.payload.response.RetrieveTaskResponse;
import com.gtcafe.rws.booter.payload.standard.response.StandardErrorResponse;
import com.gtcafe.rws.booter.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Task", description = "Async Task")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	Logger logger = LoggerFactory.getLogger(TaskController.class);

	@Autowired
	private TaskService service;


	// ------------------------------------------------------------------------
	@Operation(
		summary = "Create a new task",
		description = "Create a new task"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = CreateTaskResponse.class), mediaType = "application/json")
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
	public ResponseEntity<CreateTaskResponse> createEntity(
			@RequestBody
			@Validated
			CreateTaskRequest request) {

		CreateTaskResponse res = service.create(request);

		return ResponseEntity.ok(res);
	}

	// ------------------------------------------------------------------------
	@Operation(
		summary = "Retrieve the task",
		description = "Retrieve the task"
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			content = {
				@Content(schema = @Schema(implementation = RetrieveTaskResponse.class), mediaType = "application/json")
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
	public ResponseEntity<RetrieveTaskResponse> retrieveEntityById(@PathVariable String id) {

		RetrieveTaskResponse response = service.retrieveEntityById(id);

		return ResponseEntity.ok(response);
	}


}
