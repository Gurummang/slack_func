package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.file.SlackFileService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.GASB.slack_func.service.SlackUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/connect/slack/")
public class SlackInfoController {

    private final SlackChannelService slackChannelService;
    private final SlackUserService slackUserService;
    private final SlackSpaceInfoService slackSpaceInfoService;
    private final SlackFileService slackFileService;

    @Autowired
    public SlackInfoController(SlackChannelService slackChannelService,
                               SlackUserService slackUserService,
                               SlackSpaceInfoService slackSpaceInfoService,
                               SlackFileService slackFileService) {
        this.slackChannelService = slackChannelService;
        this.slackUserService = slackUserService;
        this.slackSpaceInfoService = slackSpaceInfoService;
        this.slackFileService = slackFileService;
    }

    @PostMapping("/channels")
    public ResponseEntity<Map<String, String>> fetchAndSaveChannels() {
        Map<String, String> response = new HashMap<>();
        try {
            slackChannelService.slackFirstChannels();
            response.put("status", "success");
            response.put("message", "Channels saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching conversations");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> fetchAndSaveUsers() {
        Map<String, String> response = new HashMap<>();
        try {
            slackUserService.slackFirstUsers();
            response.put("status", "success");
            response.put("message", "Users saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching users");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/files")
    public ResponseEntity<Map<String, String>> fetchAndSaveFiles() {
        Map<String, String> response = new HashMap<>();
        try {
            slackFileService.fetchAndStoreFiles();
            response.put("status", "success");
            response.put("message", "Files saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching files");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/space")
    public ResponseEntity<Map<String, String>> fetchAndSaveSpaceInfo() {
        Map<String, String> response = new HashMap<>();
        try {
            slackSpaceInfoService.slackSpaceRegister();
            response.put("status", "success");
            response.put("message", "Space info saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching space info");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> fetchAndSaveAll() {
        Map<String, String> response = new HashMap<>();
        try {
            slackSpaceInfoService.slackSpaceRegister();
            slackChannelService.slackFirstChannels();
            slackUserService.slackFirstUsers();
            slackFileService.fetchAndStoreFiles();
            response.put("status", "success");
            response.put("message", "All data saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching all data");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
