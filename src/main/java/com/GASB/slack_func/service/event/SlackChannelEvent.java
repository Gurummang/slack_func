package com.GASB.slack_func.service.event;

import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackChannelService;
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


    public void handleChannelEvent(Map<String, Object> payload) {
        log.info("Handling channel event");
        try {
            Conversation new_conversation = slackApiService.fetchConversationInfo(payload.get("channelId").toString());
            slackChannelService.addChannel(new_conversation);
            log.info("Channel event processed successfully");
        } catch (Exception e) {
            log.error("Unexpected error processing channel event", e);
        }
    }
}
