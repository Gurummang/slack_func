package com.GASB.slack_func.repository.channel;

import com.GASB.slack_func.model.dto.SlackChannelDto;

import java.util.List;

public interface SlackChannelInterface {
    void saveChannelList(List<SlackChannelDto> channelListDtos);
}
