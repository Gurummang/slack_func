package com.GASB.slack_func.service.file;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.AV.DlpRepo;
import com.GASB.slack_func.repository.AV.FileStatusRepository;
import com.GASB.slack_func.repository.AV.VtReportRepository;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.org.WorkspaceConfigRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUtil {

    private final SlackFileRepository storedFilesRepository;
    private final FileUploadRepository fileUploadRepository;
    private final FileActivityRepo activitiesRepository;
    private final SlackUserRepo slackUserRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final SlackChannelRepository slackChannelRepository;
    private final SlackFileMapper slackFileMapper;
    private final RestTemplate restTemplate;
    private final DlpRepo dlpRepo;
    private final VtReportRepository vtReportRepository;
    private final FileStatusRepository fileStatusRepository;
    private final S3Client s3Client;
    private final RabbitTemplate rabbitTemplate;
    private final WorkspaceConfigRepo worekSpaceRepo;
    private final ScanUtil scanUtil;

    @Value("${aws.s3.bucket}")
    private String bucketName;
    private static final String HASH_ALGORITHM = "SHA-256";

    @Async("threadPoolTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processAndStoreFile(File file, OrgSaaS orgSaaSObject, int workspaceId) {
        return downloadFileAsync(file.getUrlPrivateDownload(), getToken(workspaceId))
                .thenApply(fileData -> {
                    try {
                        return handleFileProcessing(file, orgSaaSObject, fileData, workspaceId);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException("File processing failed", e);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error processing file: {}", file.getName(), ex);
                    return null;
                });
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<byte[]> downloadFileAsync(String fileUrl, String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadFile(fileUrl, token);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private byte[] downloadFile(String fileUrl, String token) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new IOException("Failed to download file from URL: " + fileUrl);
            }
        } catch (RestClientException e) {
            log.error("Error downloading file from URL {}: {}", fileUrl, e.getMessage(), e);
            throw new IOException("Error downloading file", e);
        }
    }

    private Void handleFileProcessing(File file, OrgSaaS orgSaaSObject, byte[] fileData, int workspaceId) throws IOException, NoSuchAlgorithmException {
        String hash = calculateHash(fileData);
        String workspaceName = worekSpaceRepo.findById(workspaceId).get().getWorkspaceName();

        // 채널 및 사용자 정보 가져오기
        String channelId = getFirstChannelId(file);
        String userId = file.getUser();

        String channelName = fetchChannelName(channelId);
        String uploadedUserName = fetchUserName(userId);

        MonitoredUsers user = fetchUserById(userId);
        if (user == null) return null;

        String saasName = orgSaaSObject.getSaas().getSaasName();
        String orgName = orgSaaSObject.getOrg().getOrgName();
        String filePath = saveFileToLocal(fileData, saasName, workspaceName, channelName, hash, file.getTitle());

        // 저장 경로 설정
        String uploadedChannelPath = String.format("%s/%s/%s/%s/%s", orgName, saasName, workspaceName, channelName, uploadedUserName);
        String s3Key = String.format("%s/%s/%s/%s/%s/%s", orgName, saasName, workspaceName, channelName, hash, file.getTitle());

        StoredFile storedFile = slackFileMapper.toStoredFileEntity(file, hash, s3Key);
        fileUpload fileUploadObject = slackFileMapper.toFileUploadEntity(file, orgSaaSObject, hash);
        Activities activity = slackFileMapper.toActivityEntity(file, "file_uploaded", user);
        activity.setUploadChannel(uploadedChannelPath);


        synchronized (this) {
            // 활동 및 파일 업로드 정보 저장 (중복 체크 후 저장)
            if (activityDuplicate(activity)) {
                activitiesRepository.save(activity);
            } else {
                log.warn("Duplicate activity detected and ignored: {}", file.getName());
            }

            if (fileUploadDuplicate(fileUploadObject)) {
                fileUploadRepository.save(fileUploadObject);
            } else {
                log.warn("Duplicate file upload detected and ignored: {}", file.getName());
            }

            if (isFileNotStored(storedFile)) {
                try {
                    storedFilesRepository.save(storedFile);
                    sendMessage(storedFile.getId());
                    log.info("File uploaded successfully: {}", file.getName());
                } catch (DataIntegrityViolationException e) {
                    log.warn("Duplicate entry detected and ignored: {}", file.getName());
                }
            } else {
                log.warn("Duplicate file detected: {}", file.getName());
            }
        }
        scanUtil.scanFile(filePath, fileUploadObject, file.getMimetype(), file.getFiletype());
        uploadFileToS3(filePath, s3Key);

        return null;
    }

    private boolean isFileNotStored(StoredFile storedFile) {
        return storedFilesRepository.findBySaltedHash(storedFile.getSaltedHash()).isEmpty();
    }

    private boolean fileUploadDuplicate(fileUpload fileUploadObject) {
        String fild_id = fileUploadObject.getSaasFileId();
        LocalDateTime event_ts = fileUploadObject.getTimestamp();
        return fileUploadRepository.findBySaasFileIdAndTimestamp(fild_id, event_ts).isEmpty();
    }

    private boolean activityDuplicate(Activities activity) {
        String fild_id = activity.getSaasFileId();
        LocalDateTime event_ts = activity.getEventTs();
        return activitiesRepository.findBySaasFileIdAndEventTs(fild_id, event_ts).isEmpty();
    }

    public static String calculateHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hash = digest.digest(fileData);
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex.length() == 1 ? "0" : "").append(hex);
        }
        return hexString.toString();
    }

    private String saveFileToLocal(byte[] fileData, String saasName, String workspaceName, String channelName, String hash, String fileName) throws IOException {
        saasName = sanitizePathSegment(saasName);
        workspaceName = sanitizePathSegment(workspaceName);
//        channelName = sanitizePathSegment(channelName);
        fileName = sanitizeFileName(fileName);

        Path basePath = Paths.get("downloaded_files");
        Path filePath = basePath.resolve(Paths.get(saasName, workspaceName, channelName, hash, fileName));

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
        return filePath.toString();
    }

    private void uploadFileToS3(String filePath, String s3Key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.putObject(putObjectRequest, Paths.get(filePath));
            log.info("File uploaded successfully to S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
        }
    }

    private String fetchChannelName(String channelId) {
        if (channelId == null) return "unknown_channel";
        Optional<ChannelList> channel = slackChannelRepository.findByChannelId(channelId);
        return channel.map(ChannelList::getChannelName).orElse("unknown_channel");
    }

    private String fetchUserName(String userId) {
        if (userId == null) return "unknown_user";
        Optional<MonitoredUsers> user = slackUserRepo.findByUserId(userId);
        return user.map(MonitoredUsers::getUserName).orElse("unknown_user");
    }

    private MonitoredUsers fetchUserById(String userId) {
        Optional<MonitoredUsers> userOptional = slackUserRepo.findByUserId(userId);
        if (userOptional.isEmpty()) {
            log.error("User with ID {} not found", userId);
            return null;
        }
        return userOptional.get();
    }

    private OrgSaaS fetchOrgSaaSByUser(MonitoredUsers user) {
        Optional<OrgSaaS> saasOptional = orgSaaSRepo.findById(user.getOrgSaaS().getId());
        if (saasOptional.isEmpty()) {
            log.error("OrgSaaS for user {} not found", user.getUserId());
            return null;
        }
        return saasOptional.get();
    }


    private static String sanitizePathSegment(String segment) {
        return segment.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9-_.]", "_");
    }

    private String getFirstChannelId(File file) {
        return file.getChannels().isEmpty() ? null : file.getChannels().get(0);
    }

    public String TokenSelector(OrgSaaS orgSaaSObject) {
        WorkspaceConfig workspaceConfig = worekSpaceRepo.findById(orgSaaSObject.getId()).get();
        return workspaceConfig.getToken();
    }

    public String getToken(int workespaceId) {
        return worekSpaceRepo.findById(workespaceId)
                .orElseThrow(() -> new NoSuchElementException("No token found for spaceId: " + workespaceId))
                .getToken();
    }


    protected int calculateTotalFileSize(List<fileUpload> targetFileList) {
        log.info("targetFileList: {}", targetFileList);
        return targetFileList.stream()
                .map(file -> storedFilesRepository.findBySaltedHash(file.getHash()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToInt(storedFile -> Optional.of(storedFile.getSize())
                        .orElse(0))
                .sum();
    }

    public int CalcSlackSensitiveSize(List<fileUpload> TargetFileList) {
        return getSensitiveFileList(TargetFileList).stream()
                .mapToInt(StoredFile::getSize).sum();
    }

    public int CalcSlackMaliciousSize(List<fileUpload> TargetFileList) {
        return getMaliciousFileList(TargetFileList).stream()
                .mapToInt(StoredFile::getSize).sum();
    }

    public List<StoredFile> getMaliciousFileList(List<fileUpload> targetFileList) {
        return targetFileList.stream()
                .map(file -> storedFilesRepository.findBySaltedHash(file.getHash()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storedFile -> vtReportRepository.findByStoredFile(storedFile)
                        .map(vtReport -> !vtReport.getThreatLabel().equals("none"))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    private List<StoredFile> getSensitiveFileList(List<fileUpload> targetFileList) {
        return targetFileList.stream()
                .map(file -> storedFilesRepository.findBySaltedHash(file.getHash()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storedFile -> dlpRepo.findByStoredFile(storedFile)
                        .map(DlpReport::getDlp)
                        .orElse(false))
                .collect(Collectors.toList());
    }

    public int countSensitiveFiles(List<fileUpload> targetFileList) {
        return (int) targetFileList.stream()
                .map(file -> storedFilesRepository.findBySaltedHash(file.getHash()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storedFile -> {
                    FileStatus fileStatus = fileStatusRepository.findByStoredFile(storedFile);
                    return fileStatus != null && fileStatus.getDlpStatus() == 1 &&
                            dlpRepo.findByStoredFile(storedFile)
                                    .map(DlpReport::getDlp)
                                    .orElse(false);
                })
                .count();
    }

    public int countMaliciousFiles(List<fileUpload> targetFileList) {
        return (int) targetFileList.stream()
                .map(file -> storedFilesRepository.findBySaltedHash(file.getHash()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storedFile -> {
                    FileStatus fileStatus = fileStatusRepository.findByStoredFile(storedFile);
                    return fileStatus != null && fileStatus.getGscanStatus() == 1 &&
                            vtReportRepository.findByStoredFile(storedFile)
                                    .map(vtReport -> !vtReport.getThreatLabel().equals("none"))
                                    .orElse(false);
                })
                .count();
    }

    public int countConnectedAccounts(OrgSaaS orgSaaSObject) {
        return slackUserRepo.findByOrgSaaS(orgSaaSObject).size();
    }

    public void sendMessage(Long message) {
        rabbitTemplate.convertAndSend(message);
        System.out.println("Sent message: " + message);
    }


}
