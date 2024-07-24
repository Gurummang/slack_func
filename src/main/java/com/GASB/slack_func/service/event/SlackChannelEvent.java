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
            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceId(payload.get("teamId").toString()).get();
            String token = fileUtil.TokenSelector(orgSaaSObject);
            Conversation new_conversation = slackApiService.fetchConversationInfo(payload.get("channelId").toString(),orgSaaSObject);
            slackChannelService.addChannel(new_conversation,orgSaaSObject);
            log.info("Channel event processed successfully");
        } catch (Exception e) {
            log.error("Unexpected error processing channel event", e);
        }
    }
}
