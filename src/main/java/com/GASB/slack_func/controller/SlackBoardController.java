package com.GASB.slack_func.controller;

import com.GASB.slack_func.annotation.SlackBoardGroup;
import com.GASB.slack_func.configuration.ExtractData;
import com.GASB.slack_func.model.dto.TopUserDTO;
import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.Saas;
import com.GASB.slack_func.repository.org.AdminRepo;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.org.SaasRepo;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final SlackFileService slackFileService;
    private final OrgSaaSRepo  orgSaaSRepo;
    private final SaasRepo saasRepo;
    private final AdminRepo adminRepo;
    private final SlackFileService fileService;
    private final SlackUserService slackUserService;

    @PostMapping("/files/size")
    public ResponseEntity<SlackFileSizeDto> fetchFileSize(@RequestBody @Validated(SlackBoardGroup.class) ExtractData request){
        try{
            String email = request.getEmail();
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();

//            List<OrgSaaS> orgSaaSList = orgSaaSRepo.findAllByOrgIdAndSaas(orgId,saasRepo.findBySaasName("Slack").orElse(null));
//            log.info("orgSaaSList: {}", orgSaaSList);
            SlackFileSizeDto slackFileSizeDto = slackFileService.SumOfSlackFileSize(orgId,1);
            return ResponseEntity.ok(slackFileSizeDto);
        } catch (Exception e) {
            log.error("Error fetching file size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileSizeDto(0,0,0));
        }
    }

    @PostMapping("/files/count")
    public ResponseEntity<SlackFileCountDto> fetchFileCount(@RequestBody @Validated(SlackBoardGroup.class) ExtractData request){

        try{
            String email = request.getEmail();
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
//            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
//            List<OrgSaaS> orgSaaSList = orgSaaSRepo.findAllByOrgIdAndSaas(orgId,saasObject);
            SlackFileCountDto slackFileCountDto = slackFileService.testCountSum(orgId,1);
            return ResponseEntity.ok(slackFileCountDto);
        } catch (Exception e) {
            log.error("Error fetching file count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileCountDto(0,0,0,0));
        }
    }
    @PostMapping("/files/recent")
    public ResponseEntity<List<SlackRecentFileDTO>> fetchRecentFiles(@RequestBody @Validated(SlackBoardGroup.class) ExtractData request) {
        try {
            String email = request.getEmail();
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            List<SlackRecentFileDTO> recentFiles = fileService.slackRecentFiles(orgId, saasObject.getId().intValue());
            return ResponseEntity.ok(recentFiles);
        } catch (Exception e) {
            log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new SlackRecentFileDTO("Error", "Server Error", "N/A", LocalDateTime.now())));
        }
    }

    @PostMapping("/user-ranking")
    public ResponseEntity<List<TopUserDTO>> fetchUserRanking(@RequestBody @Validated(SlackBoardGroup.class) ExtractData request) {
        try {
            String email = request.getEmail();
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            List<TopUserDTO> topuser = slackUserService.getTopUsers(orgId, saasObject.getId().intValue());
            return ResponseEntity.ok(topuser);
        } catch (Exception e) {
            log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new TopUserDTO("Error", 0L, 0L, LocalDateTime.now())));
        }
    }

}
