package com.GASB.slack_func.repository;

import com.GASB.slack_func.dto.SlackChannelDto;

import java.util.List;

public interface SlackChannelInterface {
    void saveChannelList(List<SlackChannelDto> channelListDtos);
}
