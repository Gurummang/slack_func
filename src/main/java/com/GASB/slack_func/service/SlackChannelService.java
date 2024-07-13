package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.ChannelList;
import com.GASB.slack_func.mapper.SlackChannelMapper;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SlackChannelService {
    private final SlackApiService slackApiService;
    private final SlackChannelMapper slackChannelMapper;
    private final SlackChannelRepository slackChannelRepository;

    @Autowired
    public SlackChannelService(SlackApiService slackApiService, SlackChannelMapper slackChannelMapper, SlackChannelRepository slackChannelRepository) {
        this.slackApiService = slackApiService;
        this.slackChannelMapper = slackChannelMapper;
        this.slackChannelRepository = slackChannelRepository;
    }

    public void slackFirstChannels() {
        try {
            List<Conversation> conversations = slackApiService.fetchConversations();
            List<ChannelList> slackChannels = slackChannelMapper.toEntity(conversations);

            // 중복된 channel_id를 제외하고 저장할 채널 목록 생성
            List<ChannelList> filteredChannels = slackChannels.stream()
                    .filter(channel -> !slackChannelRepository.existsByChannelId(channel.getChannelId()))
                    .collect(Collectors.toList());

            slackChannelRepository.saveAll(filteredChannels);
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
            log.error("Error fetching conversations", e);
        }
    }
}
