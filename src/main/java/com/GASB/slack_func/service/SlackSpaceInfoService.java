package com.GASB.slack_func.service;

import com.GASB.slack_func.model.entity.SpaceList;
import com.GASB.slack_func.mapper.SpaceMapper;
import com.GASB.slack_func.repository.space.SpaceRepository;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@Slf4j
public class SlackSpaceInfoService {
    private final SlackApiService slackApiService;
    private final SpaceMapper spaceMapper;
    private final SpaceRepository spaceRepository;

    @Autowired
    public SlackSpaceInfoService(SlackApiService slackApiService, SpaceMapper spaceMapper, SpaceRepository spaceRepository) {
        this.slackApiService = slackApiService;
        this.spaceMapper = spaceMapper;
        this.spaceRepository = spaceRepository;
    }

    public void slackSpaceRegister() {
        try {
            Team slackTeam = slackApiService.fetchTeamInfo();

            int saasId = 1; // 예시로 고정값 사용, 실제로는 SaaSService를 통해 가져올 수 있습니다.
            SpaceList spaceList = spaceMapper.toSpaceEntity(slackTeam, saasId);
            spaceRepository.save(spaceList);
        } catch (IOException | SlackApiException e) {
            log.error("Error fetching team info", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
        }
    }

    public String getCurrentSpaceName() {
        Team slackTeam = null;
        try {
            slackTeam = slackApiService.fetchTeamInfo();
        } catch (IOException | SlackApiException e) {
            log.error("Error fetching team info", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
        }

        return slackTeam.getName();
    }

}
