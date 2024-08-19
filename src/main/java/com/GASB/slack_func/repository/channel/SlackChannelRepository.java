package com.GASB.slack_func.repository.channel;

import com.GASB.slack_func.model.entity.ChannelList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackChannelRepository extends JpaRepository<ChannelList, Long>{
    Optional<ChannelList> findByChannelId(String channelId);

    @Query("SELECT cl.orgSaas.id FROM ChannelList cl WHERE cl.channelId = :channelId")
    int findOrgSaaSIdByChannelId(@Param("channelId") String channelId);
    Optional<ChannelList> findByOrgSaasId(int orgSaasId);
    boolean existsByChannelId(String channelId);
}
