package com.GASB.slack_func.service;

import com.GASB.slack_func.dto.SlackChannelDto;
import com.GASB.slack_func.mapper.SlackChannelMapper;
import com.GASB.slack_func.repository.SlackChannelRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SlackChannelService {
    private final SlackApiService  slackApiService ;
    private final SlackChannelMapper slackChannelMapper;
    private final SlackChannelRepository slackChannelRepository;


    @Autowired
    public SlackChannelService(SlackApiService slackApiService, SlackChannelMapper slackChannelMapper, SlackChannelRepository slackChannelRepository) {
        this.slackApiService  = slackApiService ;
        this.slackChannelMapper = slackChannelMapper;
        this.slackChannelRepository = slackChannelRepository;
    }
    public void slackFirstChannels() {
        try {
            List<Conversation> conversations = slackApiService.fetchConversations();
            List<SlackChannelDto> channelDtos = slackChannelMapper.toDto(conversations);
            slackChannelRepository.saveChannelList(channelDtos);
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
            log.error("Error fetching conversations", e);
        }
    }

}
