package com.gtcafe.app.platform.task.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.gtcafe.app.platform.task.domain.TaskState;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskResponse {
    @Schema(
        description = "the unique id of the task",
        example = "123e4567-e89b-12d3-a456-426614174000",
        pattern = "UUID"
    )
    private String id;

    @Schema(
        description = "the state of the task",
        example = "pending"
    )
    private TaskState state;

    private String message;

    @Schema(
        description = "the start time of the task",
        example = "2021-08-01T00:00:00"
    )
    private LocalDateTime startTime;

    @Schema(
        description = "the finish time of the task",
        example = "2021-08-01T00:00:00"
    )
    private LocalDateTime finishTime;

}
