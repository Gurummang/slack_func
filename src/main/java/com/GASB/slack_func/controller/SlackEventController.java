package com.GASB.slack_func.controller;


import com.GASB.slack_func.service.event.SlackChannelEvent;
import com.GASB.slack_func.service.event.SlackFileEvent;
import com.GASB.slack_func.service.event.SlackUserEvent;
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

    private final SlackFileEvent slackFileEvent;
    private final SlackChannelEvent slackChannelEvent;
    private final SlackUserEvent slackUserEvent;


    @Autowired
    public SlackEventController(SlackFileEvent slackFileEvent, SlackChannelEvent slackChannelEvent, SlackUserEvent slackUserEvent) {
        this.slackFileEvent = slackFileEvent;
        this.slackChannelEvent = slackChannelEvent;
        this.slackUserEvent = slackUserEvent;
    }

    @PostMapping("/file-shared")
    public ResponseEntity<String> handleFileEvent(@RequestBody Map<String, Object> payload) {
        // Log the received event payload
        log.info("Received event payload: {}", payload);
        slackFileEvent.handleFileEvent(payload);
        return ResponseEntity.ok("File Event received and logged");
    }

    @PostMapping("/channel-created")
    public ResponseEntity<String> handleMessageEvent(@RequestBody Map<String, Object> payload) {
        // Log the received event payload
        log.info("Received event payload: {}", payload);
        slackChannelEvent.handleChannelEvent(payload);
        return ResponseEntity.ok("Event received and logged");
    }

    @PostMapping("/user-joined")
    public ResponseEntity<String> handleUserEvent(@RequestBody Map<String, Object> payload) {
        // Log the received event payload
        log.info("Received event payload: {}", payload);
        slackUserEvent.handleUserEvent(payload);
        return ResponseEntity.ok("User Event received and logged");
    }

}
