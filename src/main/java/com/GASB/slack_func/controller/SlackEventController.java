package com.GASB.slack_func.controller;


import com.GASB.slack_func.service.file.SlackFileEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events/slack")
@Slf4j
public class SlackEventController {

    private SlackFileEvent slackFileEvent;

    @Autowired
    public SlackEventController(SlackFileEvent slackFileEvent) {
        this.slackFileEvent = slackFileEvent;
    }

    @PostMapping("/file-shared")
    public ResponseEntity<String> handleFileEvent(@RequestBody Map<String, Object> payload) {
        // Log the received event payload
        log.info("Received event payload: {}", payload);
        return ResponseEntity.ok("Event received and logged");
    }

    @PostMapping("/msg-shared")
    public ResponseEntity<String> handleMessageEvent(@RequestBody Map<String, Object> payload) {
        // Log the received event payload
        log.info("Received event payload: {}", payload);
        return ResponseEntity.ok("Event received and logged");
    }

}
