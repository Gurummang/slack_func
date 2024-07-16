package com.GASB.slack_func.service.event;

import com.GASB.slack_func.model.dto.SlackFileSharedEventDto;
import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.GASB.slack_func.service.file.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class SlackFileEvent {

    private final FileUtil fileService;
    private final ObjectMapper mapper;
    private final SlackSpaceInfoService slackSpaceInfoService;
    private final SlackApiService slackApiService;

    @Autowired
    public SlackFileEvent(FileUtil fileService, ObjectMapper mapper,
                          SlackSpaceInfoService slackSpaceInfoService,
                          SlackApiService slackApiService) {
        this.fileService = fileService;
        this.mapper = mapper;
        this.slackApiService = slackApiService;
        this.slackSpaceInfoService = slackSpaceInfoService;
    }

    public void handleFileEvent(Map<String, Object> payload) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String workspaceName = slackSpaceInfoService.getCurrentSpaceName();
            File file = mapDtoToFile(payload);
            fileService.processAndStoreFile(file, workspaceName);
            log.info("File event processed successfully for file ID: {}", file.getId());
        } catch (SlackApiException e) {
            log.error("Error fetching file info from Slack API", e);
        } catch (IOException e) {
            log.error("Error processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }

    private File mapDtoToFile(Map<String, Object> payload) throws SlackApiException, IOException {
        SlackFileSharedEventDto dto = mapper.convertValue(payload, SlackFileSharedEventDto.class);
        return slackApiService.fetchFileInfo(dto.getFileId());
    }
}
