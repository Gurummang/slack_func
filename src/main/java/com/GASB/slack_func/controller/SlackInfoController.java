package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/slack")
public class SlackInfoController {

    private final SlackChannelService slackChannelService;

    @Autowired
    public SlackInfoController(SlackChannelService slackChannelService) {
        this.slackChannelService = slackChannelService;
    }

    @GetMapping("/channels")
    public ResponseEntity<String> fetchConversations() {
        try {
            slackChannelService.slackFirstChannels();
            return ResponseEntity.ok("Conversations fetched and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching conversations");
        }
    }
}
