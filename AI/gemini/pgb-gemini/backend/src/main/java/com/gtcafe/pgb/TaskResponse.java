package com.gtcafe.pgb;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskResponse {
    private String taskId;
    private String status;
    private String message;
}
