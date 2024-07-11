package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.GASB.slack_func.service.SlackUserService;
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
    private final SlackUserService slackUserService;
    private final SlackSpaceInfoService slackSpaceInfoService;

    @Autowired
    public SlackInfoController(SlackChannelService slackChannelService,
                               SlackUserService slackUserService,
                               SlackSpaceInfoService slackSpaceInfoService) {
        this.slackChannelService = slackChannelService;
        this.slackUserService = slackUserService;
        this.slackSpaceInfoService = slackSpaceInfoService;
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

    @GetMapping("/users")
    public ResponseEntity<String> fetchUsers() {
        try {
            slackUserService.slackFirstUsers();
            return ResponseEntity.ok("Users fetched and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users");
        }
    }

    @GetMapping("/team")
    public ResponseEntity<String> fetchTeamInfo() {
        try{
            slackSpaceInfoService.slackSpaceRegister();
            return ResponseEntity.ok("Team info fetched and processed successfully");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching team info");
        }
    }
}
