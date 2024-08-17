package com.GASB.slack_func.service.event;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.entity.Activities;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.files.FileUploadRepository;
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
    private final FileUploadRepository fileUploadRepository;
    private final SlackFileMapper slackFileMapper;

    public void handleFileEvent(Map<String, Object> payload, String event_type) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String spaceId = payload.get("teamId").toString();
            String fileId = payload.get("fileId").toString();

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceId(spaceId).orElse(null);
            File fileInfo = slackApiService.fetchFileInfo(fileId, orgSaaSObject.getId());
            if (fileInfo.getMode() == "quip" || fileInfo.getPrettyType() == "캔버스" || fileInfo.getPrettyType() == "canvas"){
                log.info("File is a quip or canvas file, skipping processing");
                return;
            }
            fileService.processAndStoreFile(fileInfo, orgSaaSObject, orgSaaSObject.getId(), event_type);

            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException | IOException e) {
            log.error("Error fetching file info or processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }

    public void handleFileDeleteEvent(Map<String, Object> payload) {
        // 1. activities 테이블에 deleted 이벤트로 추가
        String file_id = payload.get("fileId").toString();
        String user_id = payload.get("userId").toString();
        Activities activities = slackFileMapper.toActivityEntitiyForDeleteEvent(file_id, "file_delete", user_id);
        // 2. file_upload 테이블에서 deleted 컬럼을 true로 변경
        fileUploadRepository.checkDelete(payload.get("fileId").toString());
    }
}
