package com.GASB.slack_func.repository;

import com.GASB.slack_func.entity.ChannelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlackChannelRepository extends JpaRepository<ChannelList, Long>, SlackChannelInterface{
}
