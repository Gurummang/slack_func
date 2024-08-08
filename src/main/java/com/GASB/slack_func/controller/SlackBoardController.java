package com.GASB.slack_func.controller;

import com.GASB.slack_func.annotation.JWT.ValidateJWT;
import com.GASB.slack_func.model.dto.TopUserDTO;
import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.Saas;
import com.GASB.slack_func.repository.org.AdminRepo;
import com.GASB.slack_func.repository.org.SaasRepo;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/api/v1/board/slack")
public class SlackBoardController {
    private final SlackFileService slackFileService;
    private final SaasRepo saasRepo;
    private final AdminRepo adminRepo;
    private final SlackFileService fileService;
    private final SlackUserService slackUserService;

    @Autowired
    public SlackBoardController(SlackFileService slackFileService, SaasRepo saasRepo, AdminRepo adminRepo, SlackFileService fileService, SlackUserService slackUserService) {
        this.slackFileService = slackFileService;
        this.saasRepo = saasRepo;
        this.adminRepo = adminRepo;
        this.fileService = fileService;
        this.slackUserService = slackUserService;
    }
    

    @GetMapping("/files/size")
    @ValidateJWT
    public ResponseEntity<SlackFileSizeDto> fetchFileSize(HttpServletRequest servletRequest){
        try{
            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileSizeDto slackFileSizeDto = slackFileService.SumOfSlackFileSize(orgId,1);
            return ResponseEntity.ok(slackFileSizeDto);
        } catch (Exception e) {
            // log.error("Error fetching file size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileSizeDto(0,0,0));
        }
    }

    @GetMapping("/files/count")
    @ValidateJWT
    public ResponseEntity<SlackFileCountDto> fetchFileCount(HttpServletRequest servletRequest){
        try{
            String email = (String) servletRequest.getAttribute("email");
            // log.info("httpServletRequest: {}", servletRequest);
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileCountDto slackFileCountDto = slackFileService.testCountSum(orgId,1);
            return ResponseEntity.ok(slackFileCountDto);
        } catch (Exception e) {
            // log.error("Error fetching file count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileCountDto(0,0,0,0));
        }
    }
    @GetMapping("/files/recent")
    @ValidateJWT
    public ResponseEntity<List<SlackRecentFileDTO>> fetchRecentFiles(HttpServletRequest servletRequest) {
        try {
            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            List<SlackRecentFileDTO> recentFiles = fileService.slackRecentFiles(orgId, saasObject.getId().intValue());
            return ResponseEntity.ok(recentFiles);
        } catch (Exception e) {
            // log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new SlackRecentFileDTO("Error", "Server Error", "N/A", LocalDateTime.now())));
        }
    }

    @GetMapping("/user-ranking")
    @ValidateJWT
    public ResponseEntity<List<TopUserDTO>> fetchUserRanking(HttpServletRequest servletRequest) {
        try {
            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            CompletableFuture<List<TopUserDTO>> future = slackUserService.getTopUsersAsync(orgId, saasObject.getId().intValue());
            List<TopUserDTO> topuser = future.get();

            return ResponseEntity.ok(topuser);
        } catch (RuntimeException e){
            // log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new TopUserDTO("Error", 0L, 0L, LocalDateTime.now())));
        } catch (Exception e) {
            // log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new TopUserDTO("Error", 0L, 0L, LocalDateTime.now())));
        }
    }

}
