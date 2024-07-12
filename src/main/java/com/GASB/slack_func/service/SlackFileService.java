package com.GASB.slack_func.service;


import com.GASB.slack_func.entity.ChannelList;
import com.GASB.slack_func.entity.MonitoredUsers;
import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SlackFileService {

    private final SlackApiService slackApiService;
    private final SlackFileRepository storedFilesRepository;
    private final SlackFileMapper slackFileMapper;
    private final FileUploadRepository fileUploadRepository;
    private final SlackChannelRepository slackChannelRepository;
    private final SlackUserRepo slackUserRepo;

    public SlackFileService(SlackApiService slackApiService,
                            SlackFileRepository storedFilesRepository,
                            SlackFileMapper slackFileMapper,
                            FileUploadRepository fileUploadRepository,
                            SlackChannelRepository slackChannelRepository,
                            SlackUserRepo slackUserRepo) {
        this.slackApiService = slackApiService;
        this.storedFilesRepository = storedFilesRepository;
        this.slackFileMapper = slackFileMapper;
        this.fileUploadRepository = fileUploadRepository;
        this.slackChannelRepository = slackChannelRepository;
        this.slackUserRepo = slackUserRepo;
    }

    public void fetchAndStoreFiles() {
        try {
            List<File> fileList = fetchFileList();
            for (File file : fileList) {
                byte[] fileData = downloadFile(file.getUrlPrivateDownload());
                String hash = calculateHash(fileData);

                // 채널 및 사용자 정보 가져오기
                String channelId = file.getChannels().isEmpty() ? null : file.getChannels().get(0);
                String userId = file.getUser();

                String workspaceName = "workspace"; // 실제로는 Slack API를 통해 workspace 이름을 가져오세요.
                String channelName = fetchChannelName(channelId);
                String uploadedUserName = fetchUserName(userId);

                String filePath = saveFileToLocal(fileData, workspaceName, channelName, uploadedUserName, file.getName());

                storedFiles storedFile = slackFileMapper.toStoredFileEntity(file, hash, filePath);
                fileUpload fileUploadObject = slackFileMapper.toFileUploadEntity(file, 1, hash);

                if (storedFilesRepository.findByFileId(storedFile.getFileId()).isEmpty()
                        && fileUploadRepository.findBySaasFileId(fileUploadObject.getSaasFileId()).isEmpty()) {
                    storedFilesRepository.save(storedFile);
                    fileUploadRepository.save(fileUploadObject);
                }
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }

    private List<File> fetchFileList() throws IOException, SlackApiException {
        return slackApiService.fetchFiles();
    }

    private byte[] downloadFile(String fileUrl) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(fileUrl, byte[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IOException("Failed to download file from URL: " + fileUrl);
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

    private String saveFileToLocal(byte[] fileData, String workspaceName, String channelName, String uploadedUserName, String fileName) throws IOException {
        Path filePath = Paths.get("downloaded_files", workspaceName, channelName, uploadedUserName, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
        return filePath.toString();
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
