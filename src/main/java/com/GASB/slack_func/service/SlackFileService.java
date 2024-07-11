package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SlackFileService {

    private final SlackApiService slackApiService;
    private final SlackFileRepository storedFilesRepository;
    private final FileUploadRepository fileUploadRepository;
    private final SlackFileMapper slackFileMapper;
    public SlackFileService(SlackApiService slackApiService,
                            SlackFileRepository storedFilesRepository,
                            FileUploadRepository fileUploadRepository,
                            SlackFileMapper slackFileMapper) {
        this.slackApiService = slackApiService;
        this.storedFilesRepository = storedFilesRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.slackFileMapper = slackFileMapper;
    }
    public void fetchAndStoreFiles() {
        try {
            List<File> fileList = slackApiService.fetchFiles();
            List<storedFiles> storedFilesList = slackFileMapper.toStoredFileEntity(fileList);
            List<storedFiles> newFiles = storedFilesList.stream()
                    .filter(file -> storedFilesRepository.findByFileId(file.getFileId()).isEmpty())
                    .collect(Collectors.toList());

            if (!newFiles.isEmpty()) {
                storedFilesRepository.saveAll(newFiles);
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error fetching files", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
        }
    }

    public void uploadFiles() {
        try{
            List<File> fileList = slackApiService.fetchFiles();
            List<fileUpload> FileUpload = slackFileMapper.toFileUploadEntity(fileList,1);
            List<fileUpload> newUpload = FileUpload.stream()
                    .filter(file-> fileUploadRepository.findBySaasFileId(file.getSaasFileId()).isEmpty())
                    .collect(Collectors.toList());
            if (!newUpload.isEmpty()){
                fileUploadRepository.saveAll(newUpload);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error uploading files", e);
        }
    }
}
