package com.GASB.slack_func.controller;

import com.GASB.slack_func.annotation.JWT.ValidateJWT;
import com.GASB.slack_func.model.dto.TopUserDTO;
import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.Saas;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.org.AdminRepo;
import com.GASB.slack_func.repository.org.SaasRepo;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
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
    private final FileUploadRepository fileUploadRepository;

    private final SlackFileRepository slackFileRepository;

    @Autowired
    public SlackBoardController(SlackFileService slackFileService, SaasRepo saasRepo
            , AdminRepo adminRepo, SlackFileService fileService
            , SlackUserService slackUserService, FileUploadRepository fileUploadRepository,
                                SlackFileRepository slackFileRepository) {
        this.slackFileService = slackFileService;
        this.saasRepo = saasRepo;
        this.adminRepo = adminRepo;
        this.fileService = fileService;
        this.slackUserService = slackUserService;
        this.fileUploadRepository = fileUploadRepository;
        this.slackFileRepository = slackFileRepository;
    }
    

    @GetMapping("/files/size")
    @ValidateJWT
    public ResponseEntity<?> fetchFileSize(HttpServletRequest servletRequest){
        try{
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching file size in size api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileSizeDto slackFileSizeDto = slackFileService.sumOfSlackFileSize(orgId,1);
            return ResponseEntity.ok(slackFileSizeDto);
        } catch (Exception e) {
            // log.error("Error fetching file size", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileSizeDto(0,0,0));
        }
    }

    @GetMapping("/files/count")
    @ValidateJWT
    public ResponseEntity<?> fetchFileCount(HttpServletRequest servletRequest){
        try{
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching file count in count api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = (String) servletRequest.getAttribute("email");
             log.info("httpServletRequest: {}", servletRequest);
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            SlackFileCountDto slackFileCountDto = slackFileService.SlackFileCountSum(orgId,1);
            return ResponseEntity.ok(slackFileCountDto);
        } catch (Exception e) {
             log.error("Error fetching file count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SlackFileCountDto(0,0,0,0));
        }
    }
    @GetMapping("/files/recent")
    @ValidateJWT
    public ResponseEntity<?> fetchRecentFiles(HttpServletRequest servletRequest) {
        try {
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching recent files in recent api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
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
    public ResponseEntity<?> fetchUserRanking(HttpServletRequest servletRequest) {
        try {
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching user ranking in user-ranking api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            CompletableFuture<List<TopUserDTO>> future = slackUserService.getTopUsersAsync(orgId, saasObject.getId().intValue());
            List<TopUserDTO> topuser = future.get();

            return ResponseEntity.ok(topuser);
        } catch (Exception e) {
            // log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new TopUserDTO("Error", 0L, 0L, LocalDateTime.now())));
        }
    }

    @PostMapping("/files/download")
    @ValidateJWT
    public ResponseEntity<?> downloadFile(HttpServletRequest servletRequest, @RequestBody Map<String, String> request) {
        try {
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error downloading file in downloadFile API: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // JWT에서 이메일 추출
            String email = (String) servletRequest.getAttribute("email");
            log.info("email : {}", email);

            // 요청에서 file_name 가져오기
            String fileName = request.get("file_name");

            // 파일 이름 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(fileName);
            String decodedFileName = new String(decodedBytes, StandardCharsets.UTF_8);
            log.info("decodedFileName : {}", decodedFileName);

            // 파일 이름에서 해시값 추출
            String[] fileInfo = decodedFileName.split("-");
            if (fileInfo.length < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file name format");
            }
            String saltedHash = fileInfo[1];
            log.info("saltedHash : {}", saltedHash);

            // 파일 존재 여부 확인
            boolean fileExists = fileUploadRepository.existsByUserAndHash(email, 1, saltedHash);
            String orgName = adminRepo.findByEmail(email).get().getOrg().getOrgName();

            if (!fileExists) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("File not found or access denied");
            }

            // 해시값으로 파일 저장 경로 검사
            String fileSavePath = slackFileRepository.findSavePathBySaltedHash(saltedHash).orElse(null);
            if (fileSavePath == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File path not found");
            }

            // 파일 경로에서 상대 경로 추출
            String baseDirectory = "slack-file-storage/" + orgName + "/";
            if (fileSavePath.startsWith(baseDirectory)) {
                String relativePath = fileSavePath.substring(baseDirectory.length());

                // 다운로드 파일 경로 설정 (download_files 디렉토리로 변경)
                Path downloadDirectory = Paths.get("downloaded_files").toAbsolutePath().normalize();
                Path filePath = downloadDirectory.resolve(relativePath).normalize();
                log.info("filePath : {}", filePath);

                // 파일이 존재하고 읽을 수 있는지 확인
                if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or not readable");
                }

                // 리소스 생성 및 반환
                Resource resource = new UrlResource(filePath.toUri());
                if (!resource.exists() || !resource.isReadable()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found or not readable");
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file path format");
            }
        } catch (Exception e) {
            log.error("Error occurred while downloading the file", e);  // 예외 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @PostMapping("/files/delete")
    @ValidateJWT
    public ResponseEntity<?> deleteFile(HttpServletRequest servletRequest, @RequestBody Map<String, String> request) {
        // 아마 delete에는 해시값이 필요하지 않을까..?
        try {
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching user ranking in user-ranking api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String email = (String) servletRequest.getAttribute("email");
            int orgId = adminRepo.findByEmail(email).get().getOrg().getId();
            Saas saasObject = saasRepo.findBySaasName("Slack").orElse(null);
            Map<String, String> response = new HashMap<>();


            response.put("status","200");
            response.put("message","file deleted");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // log.error("Error fetching recent files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(new TopUserDTO("Error", 0L, 0L, LocalDateTime.now())));
        }
    }
}
