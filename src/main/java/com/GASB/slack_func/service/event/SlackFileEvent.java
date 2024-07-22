package com.GASB.slack_func.service.event;

import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.service.SlackApiService;
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
    private final SlackApiService slackApiService;
    private final OrgSaaSRepo orgSaaSRepo;

    public void handleFileEvent(Map<String, Object> payload) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String teamId = payload.get("team_id").toString();
            String fileId = payload.get("file_id").toString();

            String slackSpaceName = orgSaaSRepo.findBySpaceId(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid team ID: " + teamId))
                    .getConfig().getSaasname();

            File fileInfo = slackApiService.fetchFileInfo(fileId);
            fileService.processAndStoreFile(fileInfo, slackSpaceName);

            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException | IOException e) {
            log.error("Error fetching file info or processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }
}
