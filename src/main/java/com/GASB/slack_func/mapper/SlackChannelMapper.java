package com.GASB.slack_func.mapper;

import com.GASB.slack_func.model.entity.ChannelList;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.slack.api.model.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackChannelMapper {

    public List<ChannelList> toEntity(List<Conversation> conversations, OrgSaaS orgSaas) {
        return conversations.stream().map(conversation -> toEntity(conversation, orgSaas)).collect(Collectors.toList());
    }

    public ChannelList toEntity(Conversation conversation, OrgSaaS orgSaas) {
        return ChannelList.builder()
                .channelId(conversation.getId())
                .channelName(conversation.getName())
                .orgSaas(orgSaas) // 새로 추가된 필드 매핑
                .build();
    }
}
