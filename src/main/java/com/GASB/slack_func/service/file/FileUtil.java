package com.GASB.slack_func.service.file;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.org.WorkspaceConfigRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.GASB.slack_func.service.AESUtil;
import com.GASB.slack_func.service.MessageSender;
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FileUtil {

    private final SlackFileRepository storedFilesRepository;
    private final FileUploadRepository fileUploadRepository;
    private final FileActivityRepo activitiesRepository;
    private final SlackUserRepo slackUserRepo;
    private final SlackChannelRepository slackChannelRepository;
    private final SlackFileMapper slackFileMapper;
    private final RestTemplate restTemplate;
    private final S3Client s3Client;
    private final WorkspaceConfigRepo worekSpaceRepo;
    private final ScanUtil scanUtil;
    private final MessageSender messageSender;

    @Autowired
    public FileUtil(SlackFileRepository storedFilesRepository, FileUploadRepository fileUploadRepository, FileActivityRepo activitiesRepository, SlackUserRepo slackUserRepo, SlackChannelRepository slackChannelRepository, SlackFileMapper slackFileMapper, RestTemplate restTemplate, S3Client s3Client, WorkspaceConfigRepo worekSpaceRepo, ScanUtil scanUtil, MessageSender messageSender) {
        this.storedFilesRepository = storedFilesRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.activitiesRepository = activitiesRepository;
        this.slackUserRepo = slackUserRepo;
        this.slackChannelRepository = slackChannelRepository;
        this.slackFileMapper = slackFileMapper;
        this.restTemplate = restTemplate;
        this.s3Client = s3Client;
        this.worekSpaceRepo = worekSpaceRepo;
        this.scanUtil = scanUtil;
        this.messageSender = messageSender;
    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aes.key}")
    private String key;
    private static final String HASH_ALGORITHM = "SHA-256";

    @Async("threadPoolTaskExecutor")
    @Transactional
    public CompletableFuture<Void> processAndStoreFile(File file, OrgSaaS orgSaaSObject, int workspaceId, String event_type) {
        return downloadFileAsync(file.getUrlPrivateDownload(), AESUtil.decrypt(getToken(workspaceId),key))
                .thenApply(fileData -> {
                    try {
                        return handleFileProcessing(file, orgSaaSObject, fileData, workspaceId, event_type);
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

    private Void handleFileProcessing(File file, OrgSaaS orgSaaSObject, byte[] fileData, int workspaceId, String event_type) throws IOException, NoSuchAlgorithmException {
        log.info("Processing file: {}", file.getName());
        log.info("file event type : {}", event_type);
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
        FileUploadTable fileUploadTableObject = slackFileMapper.toFileUploadEntity(file, orgSaaSObject, hash);
        Activities activity = slackFileMapper.toActivityEntity(file, event_type, user,uploadedChannelPath);

        synchronized (this) {
            // 활동 및 파일 업로드 정보 저장 (중복 체크 후 저장)
            if (activityDuplicate(activity)) {
                activitiesRepository.save(activity);
            } else {
                log.warn("Duplicate activity detected and ignored: {}", file.getName());
            }

            if (fileUploadDuplicate(fileUploadTableObject)) {
                fileUploadRepository.save(fileUploadTableObject);
            } else {
                log.warn("Duplicate file upload detected and ignored: {}", file.getName());
            }

            if (isFileNotStored(storedFile)) {
                try {
                    storedFilesRepository.save(storedFile);
                    messageSender.sendMessage(storedFile.getId());
                    messageSender.sendGroupingMessage(activity.getId());
                    log.info("File uploaded successfully: {}", file.getName());
                } catch (DataIntegrityViolationException e) {
                    log.warn("Duplicate entry detected and ignored: {}", file.getName());
                }
            } else {
                log.warn("Duplicate file detected: {}", file.getName());
            }
        }
        scanUtil.scanFile(filePath, fileUploadTableObject, file.getMimetype(), file.getFiletype());
        uploadFileToS3(filePath, s3Key);

        return null;
    }

    private boolean isFileNotStored(StoredFile storedFile) {
        return storedFilesRepository.findBySaltedHash(storedFile.getSaltedHash()).isEmpty();
    }

    private boolean fileUploadDuplicate(FileUploadTable fileUploadTableObject) {
        LocalDateTime event_ts = fileUploadTableObject.getTimestamp();
        String hash = fileUploadTableObject.getHash();
        return fileUploadRepository.findByTimestampAndHash(event_ts,hash).isEmpty();
    }

    private boolean activityDuplicate(Activities activity) {
        String fild_id = activity.getSaasFileId();
        String event_type = activity.getEventType();
        LocalDateTime event_ts = activity.getEventTs();
        return activitiesRepository.findByEventTsAndEventType(event_ts,event_type).isEmpty();
    }

    public static String calculateHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        log.info("Hash value : {} ", digest);
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
        // Ensure input parameters are not null
        if (fileData == null || saasName == null || workspaceName == null || channelName == null || hash == null || fileName == null) {
            throw new IllegalArgumentException("None of the input parameters can be null");
        }

        // Sanitize path segments and file name
        String sanitizedSaasName = sanitizePathSegment(saasName);
        String sanitizedWorkspaceName = sanitizePathSegment(workspaceName);
        String sanitizedChannelName = sanitizePathSegment(channelName);
        String sanitizedHash = sanitizePathSegment(hash);
        String sanitizedFileName = sanitizeFileName(fileName);

        // Build the file path
        Path basePath = Paths.get("downloaded_files");
        Path filePath = basePath.resolve(Paths.get(sanitizedSaasName, sanitizedWorkspaceName, sanitizedChannelName, sanitizedHash, sanitizedFileName));

        // Create the necessary directories
        try {
            Files.createDirectories(filePath.getParent());
        } catch (SecurityException | IOException e) {
            log.error("Error creating directories: {}", e.getMessage(), e);
        } finally {
            log.info("Directories created: {}", filePath.getParent());
        }

        // Write the file
        Files.write(filePath, fileData);

        // Return the file path
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

    private String sanitizePathSegment(String segment) {
        if (segment == null) {
            return null;
        }
        // 경로 세그먼트에서 허용되지 않는 문자를 제거하거나 치환하는 로직 추가
        return segment.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        // 파일 이름에서 허용되지 않는 문자를 제거하거나 치환하는 로직 추가
        return FilenameUtils.getName(fileName).replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private String getFirstChannelId(File file) {
        return file.getChannels().isEmpty() ? null : file.getChannels().get(0);
    }

    public String tokenSelector(OrgSaaS orgSaaSObject) {
        WorkspaceConfig workspaceConfig = worekSpaceRepo.findById(orgSaaSObject.getId()).get();
        return workspaceConfig.getToken();
    }

    public String getToken(int workespaceId) {
        return worekSpaceRepo.findById(workespaceId)
                .orElseThrow(() -> new NoSuchElementException("No token found for spaceId: " + workespaceId))
                .getToken();
    }
}
