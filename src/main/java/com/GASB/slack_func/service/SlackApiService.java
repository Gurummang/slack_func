package com.GASB.slack_func.service;

import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.service.file.FileUtil;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.files.FilesDeleteRequest;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.files.FilesDeleteResponse;
import com.slack.api.methods.response.files.FilesListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.File;
import com.slack.api.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SlackApiService {

    private final Slack slack;
    private final FileUtil fileUtil;
    private String token;

    private final AESUtil aesUtil;

    private final FileUploadRepository fileUploadRepository;

    @Value("${aes.key}")
    private String key;

    public SlackApiService(FileUtil fileUtil, AESUtil aesUtil, FileUploadRepository fileUploadRepository) {
        this.slack = Slack.getInstance();
        this.fileUtil = fileUtil;
        this.aesUtil = aesUtil;
        this.fileUploadRepository = fileUploadRepository;
    }
    // ConversationsList API호출 메서드
    public List<Conversation> fetchConversations(int workspaceId) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.getToken(workspaceId),key);
        ConversationsListResponse conversationsResponse = slack.methods(token).conversationsList(r -> r);
        if (conversationsResponse.isOk()) {
            return conversationsResponse.getChannels();
        } else {
            throw new RuntimeException("Error fetching conversations: " + conversationsResponse.getError());
        }
    }

    // users.list API호출 메서드
    public List<User> fetchUsers(int workspaceId) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.getToken(workspaceId),key);
        UsersListResponse usersListResponse = slack.methods(token).usersList(r -> r);
        if (usersListResponse.isOk()) {
            return usersListResponse.getMembers();
        } else {
            throw new RuntimeException("Error fetching users: " + usersListResponse.getError());
        }
    }
    // files.list API호출 메서드
    public List<File> fetchFiles(int workspaceId) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.getToken(workspaceId),key);
        FilesListResponse filesListResponse = slack.methods(token).filesList(r -> r);
        if (filesListResponse.isOk()) {
            return filesListResponse.getFiles();
        } else {
            throw new RuntimeException("Error fetching files: " + filesListResponse.getError());
        }
    }

    // files.info API호출 메서드
    public File fetchFileInfo(String fileId, int workspaceId) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.getToken(workspaceId),key);
        com.slack.api.methods.response.files.FilesInfoResponse filesInfoResponse = slack.methods(token).filesInfo(r -> r.file(fileId));
        if (filesInfoResponse.isOk()) {
            return filesInfoResponse.getFile();
        } else {
            throw new RuntimeException("Error fetching file info: " + filesInfoResponse.getError());
        }
    }

    // conversations.info API호출 메서드
    public Conversation fetchConversationInfo(String channelId, OrgSaaS orgSaaSObject) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.tokenSelector(orgSaaSObject),key);
        com.slack.api.methods.response.conversations.ConversationsInfoResponse conversationsInfoResponse = slack.methods(token).conversationsInfo(r -> r.channel(channelId));
        if (conversationsInfoResponse.isOk()) {
            return conversationsInfoResponse.getChannel();
        } else {
            throw new RuntimeException("Error fetching conversation info: " + conversationsInfoResponse.getError());
        }
    }

    // users.info API호출 메서드
    public User fetchUserInfo(String userId, OrgSaaS orgSaaSObject) throws IOException, SlackApiException {
        token = aesUtil.decrypt(fileUtil.tokenSelector(orgSaaSObject),key);
        com.slack.api.methods.response.users.UsersInfoResponse usersInfoResponse = slack.methods(token).usersInfo(r -> r.user(userId));
        if (usersInfoResponse.isOk()) {
            return usersInfoResponse.getUser();
        } else {
            throw new RuntimeException("Error fetching user info: " + usersInfoResponse.getError());
        }
    }


    public boolean SlackFileDeleteApi(int workspace_id, String file_id) {
        Slack slack = Slack.getInstance();
        try {
            // Slack 파일 삭제 API 호출
            FilesDeleteResponse response = slack.methods().filesDelete(FilesDeleteRequest.builder()
                    .token(AESUtil.decrypt(fileUtil.getToken(workspace_id), key)) // 토큰을 조직 객체에서 가져옴
                    .file(file_id)
                    .build());
            // 응답이 성공인지 확인
            if (!response.isOk()) {
                System.err.println("Slack API Error: " + response.getError());
                return false;
            }
            return true;

        } catch (SlackApiException | IOException e) {
            // Slack API 에러 또는 네트워크/I/O 에러 처리
            System.err.println("Error occurred: " + e.getMessage());
            return false;
        }
    }

}
