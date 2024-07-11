package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.MonitoredUsers;
import com.GASB.slack_func.mapper.SlackUserMapper;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SlackUserServiceTest {

    @InjectMocks
    private SlackUserService slackUserService;

    @Mock
    private SlackApiService slackApiService;

    @Mock
    private SlackUserMapper slackUserMapper;

    @Mock
    private SlackUserRepo slackUserRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSlackFirstUsers() throws IOException, SlackApiException {
        List<User> mockUsers = Collections.singletonList(new User());
        List<MonitoredUsers> mockMonitoredUsers = Collections.singletonList(new MonitoredUsers());

        when(slackApiService.fetchUsers()).thenReturn(mockUsers);
        when(slackUserMapper.toEntity(mockUsers, 1)).thenReturn(mockMonitoredUsers);

        slackUserService.slackFirstUsers();

        verify(slackApiService, times(1)).fetchUsers();
        verify(slackUserMapper, times(1)).toEntity(mockUsers, 1);
        verify(slackUserRepo, times(1)).saveAll(mockMonitoredUsers);
    }
}
