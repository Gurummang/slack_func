package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackChannelService;
import com.GASB.slack_func.service.SlackFileService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.GASB.slack_func.service.SlackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@WebMvcTest(controllers = SlackInfoController.class)
public class SlackInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlackChannelService slackChannelService;

    @MockBean
    private SlackUserService slackUserService;

    @MockBean
    private SlackSpaceInfoService slackSpaceInfoService;

    @MockBean
    private SlackFileService slackFileService;

    @BeforeEach
    void setUp() {
        // 서비스 메서드 목 설정
        // 성공 시
        doNothing().when(slackChannelService).slackFirstChannels();
        doNothing().when(slackUserService).slackFirstUsers();
        doNothing().when(slackSpaceInfoService).slackSpaceRegister();
        doNothing().when(slackFileService).fetchAndStoreFiles();

        // 실패 시
        doThrow(new RuntimeException("Error fetching conversations")).when(slackChannelService).slackFirstChannels();
        doThrow(new RuntimeException("Error fetching users")).when(slackUserService).slackFirstUsers();
        doThrow(new RuntimeException("Error fetching team info")).when(slackSpaceInfoService).slackSpaceRegister();
        doThrow(new RuntimeException("Error fetching files")).when(slackFileService).fetchAndStoreFiles();
    }

    @Test
    void testFetchConversations_Success() throws Exception {
        // 성공 케이스
        Mockito.doNothing().when(slackChannelService).slackFirstChannels();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/channels")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Conversations fetched and processed successfully"));
        verify(slackChannelService).slackFirstChannels();
    }

    @Test
    void testFetchConversations_Error() throws Exception {
        // 에러 케이스
        doThrow(new RuntimeException("Error fetching conversations")).when(slackChannelService).slackFirstChannels();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/channels")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Error fetching conversations"));
    }

    @Test
    void testFetchUsers_Success() throws Exception {
        // 성공 케이스
        Mockito.doNothing().when(slackUserService).slackFirstUsers();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Users fetched and processed successfully"));
        verify(slackUserService).slackFirstUsers();
    }

    @Test
    void testFetchUsers_Error() throws Exception {
        // 에러 케이스
        doThrow(new RuntimeException("Error fetching users")).when(slackUserService).slackFirstUsers();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Error fetching users"));
    }

    @Test
    void testFetchTeamInfo_Success() throws Exception {
        // 성공 케이스
        Mockito.doNothing().when(slackSpaceInfoService).slackSpaceRegister();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Team info fetched and processed successfully"));
        verify(slackSpaceInfoService).slackSpaceRegister();
    }

    @Test
    void testFetchTeamInfo_Error() throws Exception {
        // 에러 케이스
        doThrow(new RuntimeException("Error fetching team info")).when(slackSpaceInfoService).slackSpaceRegister();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Error fetching team info"));
    }

    @Test
    void testFetchFiles_Success() throws Exception {
        // 성공 케이스
        Mockito.doNothing().when(slackFileService).fetchAndStoreFiles();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/files")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Files fetched and processed successfully"));
        verify(slackFileService).fetchAndStoreFiles();
    }

    @Test
    void testFetchFiles_Error() throws Exception {
        // 에러 케이스
        doThrow(new RuntimeException("Error fetching files")).when(slackFileService).fetchAndStoreFiles();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/files")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string("Error fetching files"));
    }

    @Test
    void testFetchAll_Success() throws Exception {
        // 성공 케이스
        Mockito.doNothing().when(slackChannelService).slackFirstChannels();
        Mockito.doNothing().when(slackUserService).slackFirstUsers();
        Mockito.doNothing().when(slackSpaceInfoService).slackSpaceRegister();
        Mockito.doNothing().when(slackFileService).fetchAndStoreFiles();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/slack/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("All fetched and processed successfully"));
        verify(slackChannelService).slackFirstChannels();
        verify(slackUserService).slackFirstUsers();
        verify(slackSpaceInfoService).slackSpaceRegister();
        verify(slackFileService).fetchAndStoreFiles();
    }
}