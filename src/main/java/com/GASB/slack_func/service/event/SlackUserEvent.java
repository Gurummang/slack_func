package com.GASB.slack_func.service.event;

import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackUserEvent {

    private final SlackApiService slackApiService;
    private final SlackUserService slackUserService;

    public void handleUserEvent(Map<String, Object> payload) {
        log.info("Handling user event");
        try {
            slackUserService.addUser(slackApiService.fetchUserInfo(payload.get("joinedUserId").toString()));
            log.info("User event processed successfully");
        } catch (Exception e) {
            log.error("Unexpected error processing user event", e);
        }
    }
}
