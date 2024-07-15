package com.GASB.slack_func.controller;

import com.GASB.slack_func.model.dto.SlackRecentFileDTO;
import com.GASB.slack_func.model.dto.SlackTotalFileDataDto;
import com.GASB.slack_func.service.file.SlackFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/file/slack")
public class SlackFileController {
    private final SlackFileService slackFileService;
    private static final Logger logger = LoggerFactory.getLogger(SlackFileController.class);

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
            logger.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new SlackRecentFileDTO("Error", "Server Error", "N/A", LocalDateTime.now())));
        }
    }

    @PostMapping("/total")
    public ResponseEntity<SlackTotalFileDataDto> fetchTotalFilesData() {
        try {
            SlackTotalFileDataDto totalFilesData = slackFileService.slackTotalFilesData();
            if (totalFilesData.getFiles().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SlackTotalFileDataDto.builder()
                        .ok(false)
                        .files(Collections.singletonList(SlackTotalFileDataDto.FileDetail.builder()
                                .fileName("No Data")
                                .username("No Data")
                                .fileType("N/A")
                                .timestamp(LocalDateTime.now())
                                .build()))
                        .build());
            }
            return ResponseEntity.ok(totalFilesData);
        } catch (Exception e) {
            logger.error("Error fetching total files data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SlackTotalFileDataDto.builder()
                    .ok(false)
                    .files(Collections.singletonList(SlackTotalFileDataDto.FileDetail.builder()
                            .fileName("Error")
                            .username("Server Error")
                            .fileType("N/A")
                            .timestamp(LocalDateTime.now())
                            .build()))
                    .build());
        }
    }

}

