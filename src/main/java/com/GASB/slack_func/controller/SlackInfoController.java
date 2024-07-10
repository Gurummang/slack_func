package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.SlackFileService;
import com.slack.api.methods.SlackApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/slack/info")
public class SlackInfoController {

    private SlackChannelService slackChannelService;

    @Autowired
    public SlackInfoController(SlackChannelService slackChannelService) {
        this.slackChannelService = slackChannelService;
    }
    @GetMapping("/channels")
    public ResponseEntity<String> fetchConversations() {
        slackChannelService.slackFirstChannels();
        return ResponseEntity.ok("Conversations fetched and processed successfully");
    }
}
