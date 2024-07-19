package com.GASB.slack_func.controller;

import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.file.SlackFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/board/slack")
public class SlackBoardController {
    private final SlackApiService slackApiService;
    private final SlackFileService slackFileService;


    @PostMapping("/files/size")
    public ResponseEntity<SlackFileSizeDto> fetchFileSize(){
        try{
            SlackFileSizeDto slackFileSizeDto = slackFileService.slackFileSize();
            return ResponseEntity.ok(slackFileSizeDto);
        } catch (Exception e) {
            log.error("Error fetching file size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileSizeDto("Error", "Server Error", "N/A", LocalDateTime.now()));
        }
    }

    @PostMapping("/files/count")
    public ResponseEntity<SlackFileCountDto> fetchFileCount(){
        try{
            SlackFileCountDto slackFileCountDto = slackFileService.slackFileCount();
            return ResponseEntity.ok(slackFileCountDto);
        } catch (Exception e) {
            log.error("Error fetching file count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileCountDto("Error", "Server Error", "N/A", LocalDateTime.now()));
        }
    }

    @PostMapping("/files/recent")
    public ResponseEntity<List<SlackRecentFileDTO>> fetchRecentFiles(){
        try {
            List<SlackRecentFileDTO> recentFiles = slackFileService.slackRecentFiles();
            return ResponseEntity.ok(recentFiles);
        } catch (Exception e) {
            log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new SlackRecentFileDTO("Error", "Server Error", "N/A", LocalDateTime.now())));
        }
    }

}
