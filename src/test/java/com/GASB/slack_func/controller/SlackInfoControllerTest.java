package com.GASB.slack_func.controller;

import com.GASB.slack_func.entity.*;
import com.GASB.slack_func.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SlackInfoController.class)
public class SlackInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SlackChannelService slackChannelService;

    @MockBean
    private SlackFileService slackFileService;

    @MockBean
    private SlackUserService slackUserService;

    @MockBean
    private SlackSpaceInfoService slackSpaceInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFetchAndSaveChannels() throws Exception {
        // Mocking
        doNothing().when(slackChannelService).slackFirstChannels();

        mockMvc.perform(get("/api/v1/slack/channels")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(slackChannelService, times(1)).slackFirstChannels();
    }

    @Test
    void testFetchAndSaveUsers() throws Exception {
        // Mocking
        doNothing().when(slackUserService).slackFirstUsers();

        mockMvc.perform(get("/api/v1/slack/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(slackUserService, times(1)).slackFirstUsers();
    }

    @Test
    void testFetchAndSaveFiles() throws Exception {
        // Mocking
        doNothing().when(slackFileService).fetchAndStoreFiles();
        doNothing().when(slackFileService).uploadFiles();

        mockMvc.perform(get("/api/v1/slack/files")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(slackFileService, times(1)).fetchAndStoreFiles();
        verify(slackFileService, times(1)).uploadFiles();
    }

    @Test
    void testFetchAndSaveSpaceInfo() throws Exception {
        // Mocking
        doNothing().when(slackSpaceInfoService).slackSpaceRegister();

        mockMvc.perform(get("/api/v1/slack/team")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(slackSpaceInfoService, times(1)).slackSpaceRegister();
    }
}
