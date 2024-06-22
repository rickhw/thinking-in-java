package com.gtcafe.mqlab01.producer.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gtcafe.mqlab01.producer.MessageProducer;
import com.gtcafe.mqlab01.producer.model.MessageEvent;
import com.gtcafe.mqlab01.producer.model.MessagePayload;

@RestController
public class RootController {

  @Autowired
  MessageProducer _producer;

  @PostMapping(value = "/send-message", produces = { "application/json" })
  public ResponseEntity<String> sendMessage(
      @RequestBody MessageEvent event) {

    String messageId = UUID.randomUUID().toString();
    event.setId(messageId);

    MessagePayload message = new MessagePayload();
    message.setMessageId(messageId);
    message.setEvent(event);

    _producer.send(message);

    return ResponseEntity.ok(String.format("sent, messageId: [%s], event: [%s]", messageId, event));
  }

}
