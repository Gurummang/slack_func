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
import com.GASB.slack_func.service.MessageSender;
import com.GASB.slack_func.service.SlackUserService;
import com.GASB.slack_func.service.file.SlackFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private final MessageSender messageSender;

    @Value("${o365.delete.url}")
    private String o365_url;

    @Value("${google.delete.url}")
    private String google_url;



    @Autowired
    public SlackBoardController(SlackFileService slackFileService, SaasRepo saasRepo
            , AdminRepo adminRepo, SlackFileService fileService
            , SlackUserService slackUserService, FileUploadRepository fileUploadRepository,
                                SlackFileRepository slackFileRepository, MessageSender messageSender) {
        this.slackFileService = slackFileService;
        this.saasRepo = saasRepo;
        this.adminRepo = adminRepo;
        this.fileService = fileService;
        this.slackUserService = slackUserService;
        this.fileUploadRepository = fileUploadRepository;
        this.slackFileRepository = slackFileRepository;
        this.messageSender = messageSender;
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException | InterruptedException | ExecutionException e) {
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
        } catch (IllegalArgumentException e) {
            log.error("Error occurred while downloading the file", e);  // 예외 로깅
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }  catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/files/delete")
    @ValidateJWT
    public ResponseEntity<?> deleteFiles(HttpServletRequest servletRequest, @RequestBody List<Map<String, String>> requests) {
        try {
            // JWT 인증 오류 처리
            if (servletRequest.getAttribute("error") != null) {
                String errorMessage = (String) servletRequest.getAttribute("error");
                Map<String, String> errorResponse = new HashMap<>();
                log.error("Error fetching delete api: {}", errorMessage);
                errorResponse.put("status", "401");
                errorResponse.put("error_message", errorMessage);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Map<String, String> response = new HashMap<>();
            List<Map<String, String>> slack_request = new ArrayList<>();
            List<Map<String, String>> o365_request = new ArrayList<>();
            List<Map<String, String>> google_drive_request = new ArrayList<>();

            boolean allSuccess = true;

            // requests null체크
            if (requests == null || requests.isEmpty()) {
                response.put("status", "400");
                response.put("message", "Request body is empty");
                return ResponseEntity.badRequest().body(response);
            }

            for (Map<String,String> each : requests){
                switch (each.get("saas")){
                    case "slack" -> slack_request.add(each);
                    case "o365" -> o365_request.add(each);
                    case "GoogleDrive" -> google_drive_request.add(each);
                }
            }

            if (slack_request.isEmpty() && o365_request.isEmpty() && google_drive_request.isEmpty()) {
                response.put("status", "400");
                response.put("message", "No valid requests found");
                return ResponseEntity.badRequest().body(response);
            }
            // 요청 받은 파일 목록 처리
            for (Map<String, String> request : slack_request) {
                int fileUploadTableIdx = Integer.parseInt(request.get("id"));
                String file_name = request.get("file_name");
                // 파일 삭제 시도
                if (!slackFileService.fileDelete(fileUploadTableIdx, file_name)) {
                    allSuccess = false;
                    log.error("Failed to delete file with id: {}", fileUploadTableIdx);
                }
            }


            if (!o365_request.isEmpty() || !google_drive_request.isEmpty()) {
                log.info("o365_request : {}", o365_request);
                log.info("google_drive_request : {}", google_drive_request);
                // o365 파일 삭제 요청
                if (!o365_request.isEmpty()) {
                    messageSender.sendO365DeleteMessage(o365_request);
                }
                // 구글 드라이브 파일 삭제 요청
                if (!google_drive_request.isEmpty()) {
                    messageSender.sendGoogleDriveDeleteMessage(google_drive_request);
                }
            }

            // 전체 성공 여부에 따른 응답
            if (allSuccess) {
                response.put("status", "200");
                response.put("message", "All files deleted successfully");
            } else {
                response.put("status", "404");
                response.put("message", "Some files failed to delete");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error deleting files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Internal server error"));
        }
    }

}
