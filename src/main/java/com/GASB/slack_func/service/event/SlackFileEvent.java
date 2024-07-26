package com.GASB.slack_func.service.event;

import com.GASB.slack_func.model.entity.OrgSaaS;
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
            String teamId = payload.get("teamId").toString();
            String fileId = payload.get("fileId").toString();

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceId(teamId).orElse(null);

//            String slackSpaceName = orgSaaSRepo.findBySpaceId(teamId)
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid team ID: " + teamId))
//                    .getConfig().getSaasname();

            File fileInfo = slackApiService.fetchFileInfo(fileId, orgSaaSObject);
            fileService.processAndStoreFile(fileInfo, orgSaaSObject);

            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException | IOException e) {
            log.error("Error fetching file info or processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }
}
