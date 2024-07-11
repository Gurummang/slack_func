package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.ChannelList;
import com.GASB.slack_func.mapper.SlackChannelMapper;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class SlackChannelServiceTest {

    @InjectMocks
    private SlackChannelService slackChannelService;

    @Mock
    private SlackApiService slackApiService;

    @Mock
    private SlackChannelMapper slackChannelMapper;

    @Mock
    private SlackChannelRepository slackChannelRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSlackFirstChannels() throws IOException, SlackApiException {
        List<Conversation> mockConversations = Collections.singletonList(new Conversation());
        List<ChannelList> mockChannels = Collections.singletonList(new ChannelList());

        when(slackApiService.fetchConversations()).thenReturn(mockConversations);
        when(slackChannelMapper.toEntity(mockConversations)).thenReturn(mockChannels);

        slackChannelService.slackFirstChannels();

        verify(slackApiService, times(1)).fetchConversations();
        verify(slackChannelMapper, times(1)).toEntity(mockConversations);
        verify(slackChannelRepository, times(1)).saveAll(mockChannels);
    }
}
