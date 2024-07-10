package com.GASB.slack_func.mapper;

import com.GASB.slack_func.dto.SlackChannelDto;
import com.GASB.slack_func.entity.ChannelList;
import com.slack.api.model.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackChannelMapper {

    // Conversation 객체를 SlackChannelDto로 변환
    public SlackChannelDto toDto(Conversation conversation) {
        return new SlackChannelDto(conversation.getId(), conversation.getName());
    }

    // Conversation 객체 리스트를 SlackChannelDto 리스트로 변환
    public List<SlackChannelDto> toDto(List<Conversation> conversations) {
        return conversations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public ChannelList toEntity(Conversation conversation) {
        return ChannelList.builder()
                .channelId(conversation.getId())
                .channelName(conversation.getName())
                .build();
    }

    // Conversation 객체 리스트를 ChannelList 엔티티 리스트로 변환
    // 예상 리턴값: List<ChannelList> 객체
    public List<ChannelList> toEntity(List<Conversation> conversations) {
        return conversations.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }


}
