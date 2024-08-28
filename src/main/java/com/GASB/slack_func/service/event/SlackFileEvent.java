package com.GASB.slack_func.service.event;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.entity.Activities;
import com.GASB.slack_func.model.entity.MonitoredUsers;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.GASB.slack_func.service.MessageSender;
import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.file.FileUtil;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final FileActivityRepo fileActivityRepo;
    private final SlackUserRepo slackUserRepo;
    private final MessageSender messageSender;

    public void handleFileEvent(Map<String, Object> payload, String event_type) {
        log.info("Handling file event with payload: {}", payload);
        try {
            String spaceId = payload.get("teamId").toString();
            String fileId = payload.get("fileId").toString();
            String event = event_type;
            if (payload.containsKey("timestamp")) {
                StringBuilder eventBuilder = new StringBuilder(event);
                event = eventBuilder.append(":").append(payload.get("timestamp").toString()).toString();
                eventBuilder = null;
            }
            log.info("event : {}", event);

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdUsingQuery(spaceId).orElse(null);
            File fileInfo = slackApiService.fetchFileInfo(fileId, orgSaaSObject.getId());
            if (fileInfo.getMode() == "quip" || fileInfo.getPrettyType() == "캔버스" || fileInfo.getPrettyType() == "canvas"){
                log.info("File is a quip or canvas file, skipping processing");
                return;
            }
            fileService.processAndStoreFile(fileInfo, orgSaaSObject, orgSaaSObject.getId(), event);

            log.info("File event processed successfully for file ID: {}", fileInfo.getId());
        } catch (SlackApiException | IOException e) {
            log.error("Error fetching file info or processing file data", e);
        } catch (Exception e) {
            log.error("Unexpected error processing file event", e);
        }
    }

    public void handleFileDeleteEvent(Map<String, Object> payload) {
       try {
           log.info("event_type : {}", payload.get("event"));
           // 1. activities 테이블에 deleted 이벤트로 추가
           String file_id = payload.get("fileId").toString();
           String event_ts = payload.get("timestamp").toString();
           String file_owner_id= null, file_name = null;

           long timestamp = Long.parseLong(event_ts.split("\\.")[0]);

           int org_saas_id = fileUploadRepository.findLatestOrgSaaSIdByFileId(file_id);


           try {
               file_owner_id = fileActivityRepo.findUserBySaasFileId(file_id).orElse(null);
           } catch (Exception e) {
               log.error("Error fetching file owner ID", e);
           }
           try {
               file_name = fileActivityRepo.findFileNamesBySaasFileId(file_id).orElse(null);
           } catch (Exception e) {
                log.error("Error fetching file name", e);
           }

           MonitoredUsers userObject = slackUserRepo.findByUserIdAndOrgSaaSId(file_owner_id, org_saas_id).orElse(null);
//           Activities activities = slackFileMapper.toActivityEntitiyForDeleteEvent(file_id, "file_delete", userObject, file_name, timestamp);
           Activities activities = copyForDelete(file_id, timestamp);
           fileActivityRepo.save(activities);
           // 2. file_upload 테이블에서 deleted 컬럼을 true로 변경
           fileUploadRepository.checkDelete(payload.get("fileId").toString());
           messageSender.sendGroupingMessage(activities.getId());
       } catch (Exception e) {
           log.error("Error processing file delete event", e);
       }
    }

    private Activities copyForDelete(String file_id, long timestamp){
        Activities activities = fileActivityRepo.findRecentBySaasFileId(file_id).orElse(null);

        return Activities.builder()
                .user(activities.getUser())
                .eventType("file_delete")
                .saasFileId(activities.getSaasFileId())
                .fileName(activities.getFileName())
                .eventTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("Asia/Seoul")))
                .uploadChannel(activities.getUploadChannel())
                .tlsh(activities.getTlsh())
                .build();
    }
}
