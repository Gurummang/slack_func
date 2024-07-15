package com.GASB.slack_func.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlackChannelDto {
    private String channelId;
    private String channelName;
}
