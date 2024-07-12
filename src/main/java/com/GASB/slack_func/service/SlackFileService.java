package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
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

@Service
@Slf4j
public class SlackFileService {

    private final SlackApiService slackApiService;
    private final SlackFileRepository storedFilesRepository;
    private final SlackFileMapper slackFileMapper;

    private final FileUploadRepository fileUploadRepository;

    public SlackFileService(SlackApiService slackApiService,
                            SlackFileRepository storedFilesRepository,
                            SlackFileMapper slackFileMapper,
                            FileUploadRepository fileUploadRepository) {
        this.slackApiService = slackApiService;
        this.storedFilesRepository = storedFilesRepository;
        this.slackFileMapper = slackFileMapper;
        this.fileUploadRepository = fileUploadRepository;
    }


//    public void uploadFiles() {
//        try{
//            List<File> fileList = slackApiService.fetchFiles();
//            List<fileUpload> FileUpload = slackFileMapper.toFileUploadEntity(fileList,1);
//            List<fileUpload> newUpload = FileUpload.stream()
//                    .filter(file-> fileUploadRepository.findBySaasFileId(file.getSaasFileId()).isEmpty())
//                    .collect(Collectors.toList());
//            if (!newUpload.isEmpty()){
//                fileUploadRepository.saveAll(newUpload);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("Error uploading files", e);
//        }
//    }
public void fetchAndStoreFiles() {
    try {
        List<File> fileList = fetchFileList();
        for (File file : fileList) {
            byte[] fileData = downloadFile(file.getUrlPrivateDownload());
            String hash = calculateHash(fileData);

            // 중복 파일 검사
            if (storedFilesRepository.findBySaltedHash(hash).isEmpty()) {
                String filePath = saveFileToLocal(fileData, file.getName());
                storedFiles storedFile = slackFileMapper.toStoredFileEntity(file, hash, filePath);
                fileUpload fileUploadobject = slackFileMapper.toFileUploadEntity(file, 1, hash);

                if (storedFilesRepository.findByFileId(storedFile.getFileId()).isEmpty()
                        && fileUploadRepository.findBySaasFileId(fileUploadobject.getSaasFileId()).isEmpty()) {
                    storedFilesRepository.save(storedFile);
                    fileUploadRepository.save(fileUploadobject);
                }
            } else {
                log.info("Duplicate file detected: " + file.getName());
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

    private String saveFileToLocal(byte[] fileData, String fileName) throws IOException {
        Path filePath = Paths.get("downloaded_files", fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
        return filePath.toString();
    }



}
