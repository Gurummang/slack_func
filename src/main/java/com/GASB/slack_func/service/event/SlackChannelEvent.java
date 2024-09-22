package com.GASB.slack_func.service.event;

import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.file.FileUtil;
import com.slack.api.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackChannelEvent {

    private final SlackChannelService slackChannelService;
    private final SlackApiService slackApiService;
    private final OrgSaaSRepo orgSaaSRepo;
    private final FileUtil fileUtil;
    
    public void handleChannelEvent(Map<String, Object> payload) {
        log.info("Handling channel event");
        try {

            String spaceId = payload.get("teamId").toString();
            String channdlId = payload.get("channelId").toString();

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdUsingQuery(spaceId).get();
            Conversation new_conversation = slackApiService.fetchConversationInfo(channdlId,orgSaaSObject);
            slackChannelService.addChannel(new_conversation,orgSaaSObject);
            log.info("Channel event processed successfully");
        } catch (RuntimeException e) {
            log.error("Unexpected error processing channel event", e);
        }
    }
}
