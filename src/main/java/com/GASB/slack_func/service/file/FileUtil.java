package com.GASB.slack_func.service.file;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.orgSaaS.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Optional;

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

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public void processAndStoreFile(File file, String workspaceName) throws IOException, NoSuchAlgorithmException {
        byte[] fileData = downloadFile(file.getUrlPrivateDownload());
        String hash = calculateHash(fileData);

        // 채널 및 사용자 정보 가져오기
        String channelId = getFirstChannelId(file);
        String userId = file.getUser();

        String channelName = fetchChannelName(channelId);
        String uploadedUserName = fetchUserName(userId);

        MonitoredUsers user = fetchUserById(userId);
        if (user == null) return;

        OrgSaaS saas = fetchOrgSaaSByUser(user);
        if (saas == null) return;

        String saasName = saas.getSaas().getSaasName();

        // 파일을 로컬에 저장하고 경로를 얻음
        String filePath = saveFileToLocal(fileData, saasName, workspaceName, channelName, file.getName());
        log.info("File saved locally at: {}", filePath);

        // 업로드된 경로 생성
        String uploadedChannelPath = String.format("%s/%s/%s/%s", saasName, workspaceName, channelName, uploadedUserName);

        // S3에 저장될 키 생성
        String s3Key = String.format("%s/%s/%s/%s", saasName, workspaceName, channelName, file.getName());

        StoredFile storedFile = slackFileMapper.toStoredFileEntity(file, hash, filePath);
        fileUpload fileUploadObject = slackFileMapper.toFileUploadEntity(file, 1, hash);
        Activities activity = slackFileMapper.toActivityEntity(file, "file_uploaded", user);
        activity.setUploadChannel(uploadedChannelPath);

        uploadFileToS3(filePath, s3Key);

        if (isFileNotStored(storedFile, fileUploadObject)) {
            storedFilesRepository.save(storedFile);
            fileUploadRepository.save(fileUploadObject);
            activitiesRepository.save(activity);
            log.info("File uploaded successfully: {}", file.getName());
        }
    }

    private byte[] downloadFile(String fileUrl) throws IOException {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(fileUrl, byte[].class);
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

    private String calculateHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(fileData);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String saveFileToLocal(byte[] fileData, String saasName, String workspaceName, String channelName, String fileName) throws IOException {
        saasName = sanitizePathSegment(saasName);
        workspaceName = sanitizePathSegment(workspaceName);
        channelName = sanitizePathSegment(channelName);
        fileName = sanitizeFileName(fileName);

        Path basePath = Paths.get("downloaded_files");
        Path filePath = basePath.resolve(Paths.get(saasName, workspaceName, channelName, fileName));

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

    private boolean isFileNotStored(StoredFile storedFile, fileUpload fileUploadObject) {
        return storedFilesRepository.findBySaltedHash(storedFile.getSaltedHash()).isEmpty()
                && fileUploadRepository.findBySaasFileId(fileUploadObject.getSaasFileId()).isEmpty();
    }

    private String sanitizePathSegment(String segment) {
        return segment.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9-_.]", "_");
    }

    private String getFirstChannelId(File file) {
        return file.getChannels().isEmpty() ? null : file.getChannels().get(0);
    }
}
