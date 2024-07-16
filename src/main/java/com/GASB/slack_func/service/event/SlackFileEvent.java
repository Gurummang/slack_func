package com.GASB.slack_func.service.event;

import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.GASB.slack_func.service.file.FileUtil;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackFileEvent {

    private final FileUtil fileService;
    private final SlackSpaceInfoService slackSpaceInfoService;
    private final SlackApiService slackApiService;

    public void handleFileEvent(Map<String, Object> payload) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String workspaceName = slackSpaceInfoService.getCurrentSpaceName();
            File fileInfo = slackApiService.fetchFileInfo(payload.get("file_id").toString());
            fileService.processAndStoreFile(fileInfo, workspaceName);
            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException e) {
            log.error("Error fetching file info from Slack API", e);
        } catch (IOException e) {
            log.error("Error processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }
//
//    private File mapDtoToFile(Map<String, Object> payload) throws SlackApiException, IOException {
//        SlackFileSharedEventDto dto = mapper.convertValue(payload, SlackFileSharedEventDto.class);
//        return slackApiService.fetchFileInfo(dto.getFileId());
//    }
}
