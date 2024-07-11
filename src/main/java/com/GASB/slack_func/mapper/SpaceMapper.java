package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.SpaceList;
import com.slack.api.model.Team;
import org.springframework.stereotype.Component;

@Component
public class SpaceMapper {

    // Team 객체를 SpaceList 엔티티로 변환
    public SpaceList toSpaceEntity(Team team, int saasId) {
        if (team == null) {
            return null;
        }
        return SpaceList.builder()
                .saasId(saasId)
                .spaceId(team.getId())
                .spaceName(team.getName())
                .spaceUrl(team.getUrl()) // team 객체에 도메인 정보가 있다고 가정합니다.
                .build();
    }
}
