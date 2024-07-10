package com.GASB.slack_func.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SlackApiService {

    private final String token;
    private final Slack slack;

    public SlackApiService(@Value("${slack.token}") String token) {
        this.token = token;
        this.slack = Slack.getInstance();
    }

    // ConversationsList API호출 메서드
    public List<Conversation> fetchConversations() throws IOException, SlackApiException {
        ConversationsListResponse conversationsResponse = slack.methods(token).conversationsList(r -> r);
        if (conversationsResponse.isOk()) {
            return conversationsResponse.getChannels();
        } else {
            throw new RuntimeException("Error fetching conversations: " + conversationsResponse.getError());
        }
    }

    // users.list API호출 메서드

    // files.list API호출 메서드

    // team.info API호출 메서드
}
