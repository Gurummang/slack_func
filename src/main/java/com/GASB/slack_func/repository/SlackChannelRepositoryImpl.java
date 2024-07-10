package com.GASB.slack_func.repository;

import com.GASB.slack_func.dto.SlackChannelDto;
import com.GASB.slack_func.entity.ChannelList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class SlackChannelRepositoryImpl implements SlackChannelInterface{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void saveChannelList(List<SlackChannelDto> channelListDtos) {
        channelListDtos.forEach(dto -> {
            ChannelList channel = ChannelList.builder()
                    .channelId(dto.getChannelId())
                    .channelName(dto.getChannelName())
                    .build();
            entityManager.persist(channel);
        });
    }
}
