package com.GASB.slack_func.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.files.FilesListResponse;
import com.slack.api.methods.response.team.TeamInfoResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.File;
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
    public List<File> fetchFiles() throws IOException, SlackApiException {
        FilesListResponse filesListResponse = slack.methods(token).filesList(r -> r);
        if (filesListResponse.isOk()) {
            return filesListResponse.getFiles();
        } else {
            throw new RuntimeException("Error fetching files: " + filesListResponse.getError());
        }
    }

    // team.info API호출 메서드
    public Team fetchTeamInfo() throws IOException, SlackApiException {
        TeamInfoResponse teamInfoResponse = slack.methods(token).teamInfo(r -> r);
        if (teamInfoResponse.isOk()) {
            return teamInfoResponse.getTeam();
        } else {
            throw new RuntimeException("Error fetching users: " + teamInfoResponse.getError());
        }
    }
}
