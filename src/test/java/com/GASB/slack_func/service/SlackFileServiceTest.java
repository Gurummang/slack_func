package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SlackFileServiceTest {

    @InjectMocks
    private SlackFileService slackFileService;

    @Mock
    private SlackApiService slackApiService;

    @Mock
    private SlackFileRepository storedFilesRepository;

    @Mock
    private FileUploadRepository fileUploadRepository;

    @Mock
    private SlackFileMapper slackFileMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFetchAndStoreFiles() throws IOException, SlackApiException {
        List<File> mockFiles = Collections.singletonList(new File());
        List<storedFiles> mockStoredFiles = Collections.singletonList(new storedFiles());

        when(slackApiService.fetchFiles()).thenReturn(mockFiles);
        when(slackFileMapper.toStoredFileEntity(mockFiles)).thenReturn(mockStoredFiles);
        when(storedFilesRepository.findByFileId(anyString())).thenReturn(Optional.empty());

        slackFileService.fetchAndStoreFiles();

        verify(slackApiService, times(1)).fetchFiles();
        verify(slackFileMapper, times(1)).toStoredFileEntity(mockFiles);
        verify(storedFilesRepository, times(1)).findByFileId(anyString());
        verify(storedFilesRepository, times(1)).saveAll(mockStoredFiles);
    }
}
