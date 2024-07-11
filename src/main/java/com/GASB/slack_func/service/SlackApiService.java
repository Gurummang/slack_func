package com.GASB.slack_func.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.team.TeamInfoResponse;
import com.slack.api.methods.response.team.TeamPreferencesListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Team;
import com.slack.api.model.User;
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
    public List<User> fetchUsers() throws IOException, SlackApiException {
        UsersListResponse usersListResponse = slack.methods(token).usersList(r -> r);
        if (usersListResponse.isOk()) {
            return usersListResponse.getMembers();
        } else {
            throw new RuntimeException("Error fetching users: " + usersListResponse.getError());
        }
    }
    // files.list API호출 메서드

    // team.info API호출 메서드
    public Team fetchTeamInfo() throws IOException, SlackApiException {
        TeamInfoResponse teamInfoResponse = slack.methods(token).teamInfo(r -> r);
        if (teamInfoResponse.isOk()) {
            return teamInfoResponse.getTeam();
        } else {
            throw new RuntimeException("Error fetching users: " + teamInfoResponse.getError());
        }
    }

//    public List<Team> fetchTeamInfo() throws IOException, SlackApiException {
//        try {
//            TeamPreferencesListResponse teamPreferencesListResponse = slack.methods(token).teamPreferencesList(r -> r);
//            if (teamPreferencesListResponse.isOk()) {
//                return teamPreferencesListResponse.getTeam();
//            } else {
//                logger.error("Error fetching team info: {}", teamPreferencesListResponse.getError());
//                throw new SlackApiException("Error fetching team info: " + teamPreferencesListResponse.getError(), null);
//            }
//        } catch (IOException | SlackApiException e) {
//            logger.error("Exception while fetching team info", e);
//            throw e; // re-throw the exception after logging it
//        }
//    }

}
