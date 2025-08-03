package com.gtcafe.messageboard.controller.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UpdateMessageRequest {
    private String content;
}