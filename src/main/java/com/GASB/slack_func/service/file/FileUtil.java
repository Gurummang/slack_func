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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    public void processAndStoreFile(File file, String workspaceName) throws IOException, NoSuchAlgorithmException {
        byte[] fileData = downloadFile(file.getUrlPrivateDownload());
        String hash = calculateHash(fileData);

        // 채널 및 사용자 정보 가져오기
        String channelId = file.getChannels().isEmpty() ? null : file.getChannels().get(0);
        String userId = file.getUser();

        String channelName = fetchChannelName(channelId);
        String uploadedUserName = fetchUserName(userId);
        MonitoredUsers user = slackUserRepo.findByUserId(userId).orElse(null);
        if (user == null) {
            log.error("User with ID {} not found", userId);
            return;
        }

        OrgSaaS saas = orgSaaSRepo.findById(user.getOrgSaaS().getId()).orElse(null);
        if (saas == null) {
            log.error("OrgSaaS for user {} not found", userId);
            return;
        }
        String saasName = saas.getSaas().getSaasName();

        // 파일을 로컬에 저장하고 경로를 얻음
        String filePath = saveFileToLocal(fileData, saasName, workspaceName, channelName, file.getName());

        // 업로드된 경로 생성
        String uploadedChannelPath = saasName + "/" + workspaceName + "/" + channelName + "/" + uploadedUserName;

        StoredFile storedFile = slackFileMapper.toStoredFileEntity(file, hash, filePath);
        fileUpload fileUploadObject = slackFileMapper.toFileUploadEntity(file, 1, hash);
        Activities activity = slackFileMapper.toActivityEntity(file, "file_uploaded", user);
        activity.setUploadChannel(uploadedChannelPath);

        if (storedFilesRepository.findBySaltedHash(storedFile.getSaltedHash()).isEmpty()
                && fileUploadRepository.findBySaasFileId(fileUploadObject.getSaasFileId()).isEmpty()) {
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
        Path basePath = Paths.get("downloaded_files");
        Path filePath = basePath.resolve(Paths.get(saasName, workspaceName, channelName, fileName));
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
        return basePath.relativize(filePath).toString().replace("\\", "/");
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
}
