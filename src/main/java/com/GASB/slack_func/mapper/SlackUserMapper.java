package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.MonitoredUsers;
import com.slack.api.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackUserMapper {

    // User 객체를 MonitoredUsers 엔티티로 변환
    public MonitoredUsers toEntity(User user, int orgSaaSId) {
        if (user == null) {
            return null;
        }
        return MonitoredUsers.builder()
                .orgSaaSId(orgSaaSId)
                .userId(user.getId())
                .userName(user.getRealName())
                .email(user.getProfile().getEmail())
                .build();
    }

    // User 리스트를 MonitoredUsers 엔티티 리스트로 변환
    public List<MonitoredUsers> toEntity(List<User> users, int orgSaaSId) {
        return users.stream()
                .map(user -> toEntity(user, orgSaaSId))
                .collect(Collectors.toList());
    }
}
