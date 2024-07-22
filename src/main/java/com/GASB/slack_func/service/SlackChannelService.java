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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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


    // 클라이언트가 넘겨주는 값은
    // space_id랑 org_id를 넘겨줄텐데.
    public void slackFirstChannels(String SpaceId,int orgId) {
        try {
            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdAndOrgId(SpaceId, orgId).get();
            
            // 이용할 토큰을 선정해서 api호출
            // 토큰은 암호화가 되어있을 테니 복호화 과정도 추가 필요
//            String used_token = fileUtil.TokenSelector(orgSaaSObject);
            // api호출
//            List<Conversation> conversations = slackApiService.fetchConversations(used_token);
            
            List<Conversation> conversations = slackApiService.fetchConversations(orgSaaSObject);
            List<ChannelList> slackChannels = slackChannelMapper.toEntity(conversations,orgSaaSObject);

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

    // 단일 채널 추가
    public void addChannel(Conversation conversation,OrgSaaS orgSaaS) {
        ChannelList channel = slackChannelMapper.toEntity(conversation, orgSaaS);
        slackChannelRepository.save(channel);
    }
}
