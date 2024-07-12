package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SlackFileServiceTest {

    @Mock
    private SlackApiService slackApiService;

    @Mock
    private SlackFileRepository storedFilesRepository;

    @Mock
    private FileUploadRepository fileUploadRepository;

    @Mock
    private SlackFileMapper slackFileMapper;

    @InjectMocks
    private SlackFileService slackFileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchAndStoreFiles() throws IOException, SlackApiException, NoSuchAlgorithmException {
        File mockFile = new File();
        mockFile.setId("123");
        mockFile.setName("test.txt");
        mockFile.setFiletype("txt");
        mockFile.setSize(100);
        mockFile.setTimestamp(1622547802);
        mockFile.setUrlPrivateDownload("https://files.slack.com/files-pri/T12345-123/download/test.txt");

        when(slackApiService.fetchFiles()).thenReturn(Collections.singletonList(mockFile));
        when(storedFilesRepository.findByFileId(anyString())).thenReturn(Optional.empty());
        when(fileUploadRepository.findBySaasFileId(anyString())).thenReturn(Optional.empty());
        when(slackFileMapper.toStoredFileEntity(any(File.class), anyString(), anyString())).thenReturn(new storedFiles());
        when(slackFileMapper.toFileUploadEntity(any(File.class), anyInt(), anyString())).thenReturn(new fileUpload());

        slackFileService.fetchAndStoreFiles();
    }
}
