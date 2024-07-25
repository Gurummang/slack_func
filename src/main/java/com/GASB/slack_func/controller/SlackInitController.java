package com.GASB.slack_func.controller;

import com.GASB.slack_func.configuration.ExtractData;
import com.GASB.slack_func.repository.org.AdminRepo;
import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final AdminRepo adminRepo;

    // 원래 여기서 AOP던 뭐던 인증을 통해서 요청한 클라이언트의 값을 받아옴
    @PostMapping("/channels")
    public ResponseEntity<Map<String, String>> fetchAndSaveChannels(@Valid @RequestBody ExtractData request) {
        String spaceId = request.getSpaceId();
        String email = request.getEmail();
        Map<String, String> response = new HashMap<>();
        int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
        try {
            slackChannelService.slackFirstChannels(spaceId, orgId); // 임시값 1
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
    public ResponseEntity<Map<String, String>> fetchAndSaveUsers(@Valid @RequestBody ExtractData request) {
        String spaceId = request.getSpaceId();
        String email = request.getEmail();
        Map<String, String> response = new HashMap<>();
        int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
        try {
            slackUserService.slackFirstUsers(spaceId, orgId);
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
    public ResponseEntity<Map<String, String>> fetchAndSaveFiles(@Valid @RequestBody ExtractData request) {
        Map<String, String> response = new HashMap<>();
        String email = request.getEmail();
        String spaceId = request.getSpaceId();
        int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
        try {
            slackFileService.fetchAndStoreFiles(spaceId, orgId);
            response.put("status", "success");
            response.put("message", "Files saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching files");
            log.error("Error fetching files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/all")
    public ResponseEntity<Map<String, String>> fetchAndSaveAll(@Valid @RequestBody ExtractData request) {
        String spaceId = request.getSpaceId();
        String email = request.getEmail();
        Map<String, String> response = new HashMap<>();
        int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
        try {
            slackChannelService.slackFirstChannels(spaceId, orgId); // 임시 org_saas_id
            slackUserService.slackFirstUsers(spaceId, orgId);
            slackFileService.fetchAndStoreFiles(spaceId, orgId);
            response.put("status", "success");
            response.put("message", "All data saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error fetching all data");
            log.error("Error fetching all data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
