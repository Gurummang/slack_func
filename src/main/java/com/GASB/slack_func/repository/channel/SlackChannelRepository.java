package com.GASB.slack_func.repository.channel;

import com.GASB.slack_func.entity.ChannelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackChannelRepository extends JpaRepository<ChannelList, Long>{
    Optional<ChannelList> findByChannelId(String channelId);
}
