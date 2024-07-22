package com.GASB.slack_func.controller;

import com.GASB.slack_func.configuration.ExtractSpaceId;
import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/connect/slack/")
public class SlackInitController {

    private final SlackChannelService slackChannelService;
    private final SlackUserService slackUserService;
    private final SlackFileService slackFileService;
    private final ExtractSpaceId extractSpaceId;

    //원래 여기서 AOP던 뭐던 인증을 통해서 요청한 클라리언트의 값을 받아옴
    @PostMapping("/channels")
    public ResponseEntity<Map<String, String>> fetchAndSaveChannels(@RequestBody ExtractSpaceId request) {
        String spaceId = request.getSpaceId();
        Map<String, String> response = new HashMap<>();
        try {
            slackChannelService.slackFirstChannels(spaceId,1); //임시값 1
            response.put("status", "success");
            response.put("message", "Channels saved successfully");
            log.info("Channels saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching conversations");
            log.error("Error fetching conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> fetchAndSaveUsers(@RequestBody ExtractSpaceId request) {
        String spaceId = request.getSpaceId();
        Map<String, String> response = new HashMap<>();
        try {
            slackUserService.slackFirstUsers(spaceId);
            response.put("status", "success");
            response.put("message", "Users saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching users");
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/files")
    public ResponseEntity<Map<String, String>> fetchAndSaveFiles(@RequestBody ExtractSpaceId request) {
        Map<String, String> response = new HashMap<>();
        String spaceId = request.getSpaceId();
        try {
            slackFileService.fetchAndStoreFiles(spaceId);
            response.put("status", "success");
            response.put("message", "Files saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching files");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> fetchAndSaveAll(@RequestBody ExtractSpaceId request) {
        String spaceId = request.getSpaceId();
        Map<String, String> response = new HashMap<>();
        try {
//            slackSpaceInfoService.slackSpaceRegister();
            slackChannelService.slackFirstChannels("T077VP0SP2M",1); //임시 org_saas_id
            slackUserService.slackFirstUsers(spaceId);
            slackFileService.fetchAndStoreFiles(spaceId);
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