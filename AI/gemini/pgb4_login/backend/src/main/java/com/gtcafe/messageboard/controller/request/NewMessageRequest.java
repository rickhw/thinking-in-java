package com.gtcafe.messageboard.controller.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewMessageRequest {
    private String userId;
    private String content;
}