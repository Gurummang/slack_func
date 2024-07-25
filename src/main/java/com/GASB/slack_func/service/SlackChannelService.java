package com.GASB.slack_func.service;

import com.GASB.slack_func.mapper.SlackChannelMapper;
import com.GASB.slack_func.model.entity.ChannelList;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.service.file.FileUtil;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackChannelService {
    private final SlackApiService slackApiService;
    private final SlackChannelMapper slackChannelMapper;
    private final SlackChannelRepository slackChannelRepository;
    private final OrgSaaSRepo orgSaaSRepo;
    private final FileUtil fileUtil;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> slackFirstChannels(String spaceId, int orgId) {
        return CompletableFuture.runAsync(() -> {
            try {
                OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdAndOrgId(spaceId, orgId)
                        .orElseThrow(() -> new RuntimeException("OrgSaaS not found"));

                List<Conversation> conversations = slackApiService.fetchConversations(orgSaaSObject);
                List<ChannelList> slackChannels = slackChannelMapper.toEntity(conversations, orgSaaSObject);

                // 중복된 channel_id를 제외하고 저장할 채널 목록 생성
                List<ChannelList> filteredChannels = slackChannels.stream()
                        .filter(channel -> !slackChannelRepository.existsByChannelId(channel.getChannelId()))
                        .collect(Collectors.toList());

                slackChannelRepository.saveAll(filteredChannels);
            } catch (IOException | SlackApiException e) {
                log.error("Error fetching conversations", e);
            }
        });
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> addChannel(Conversation conversation, OrgSaaS orgSaaS) {
        return CompletableFuture.runAsync(() -> {
            ChannelList channel = slackChannelMapper.toEntity(conversation, orgSaaS);
            if (!slackChannelRepository.existsByChannelId(channel.getChannelId())) {
                slackChannelRepository.save(channel);
            }
        });
    }
}
