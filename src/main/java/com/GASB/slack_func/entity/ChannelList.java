package com.GASB.slack_func.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChannelList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelId; // 필드명 수정: ChannelId -> channelId
    private String channelName; // 필드명 수정: ChannelName -> channelName

    @Builder
    public ChannelList(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
    }
}
