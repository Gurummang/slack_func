package com.GASB.slack_func.controller;

import com.GASB.slack_func.dto.SlackRecentFileDTO;
import com.GASB.slack_func.service.SlackFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file/slack")
public class SlackFileController {
    private final SlackFileService slackFileService;


    @Autowired
    public SlackFileController(SlackFileService slackFileService) {
        this.slackFileService = slackFileService;
    }


    @PostMapping("/recent")
    public ResponseEntity<List<SlackRecentFileDTO>> fetchRecentFiles(){
        try {
            List<SlackRecentFileDTO> recentFiles = slackFileService.slackRecentFiles();
            return ResponseEntity.ok(recentFiles);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
