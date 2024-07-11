package com.GASB.slack_func.mapper;

import com.GASB.slack_func.dto.SlackUserDto;
import com.GASB.slack_func.entity.MonitoredUsers;
import com.slack.api.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackUserMapper {

    // User -> SlackUserDto
    public SlackUserDto toUserDto(User user, int orgSaaSId) {
        return new SlackUserDto(
                user.getId(),
                orgSaaSId,
                user.getProfile().getEmail(),
                user.getProfile().getRealName(),
                user.getUpdated()
        );
    }

    // List<User> -> List<SlackUserDto>
    public List<SlackUserDto> toUserDto(List<User> users, int orgSaaSId) {
        return users.stream()
                .map(user -> toUserDto(user, orgSaaSId))
                .collect(Collectors.toList());
    }

    // SlackUserDto -> MonitoredUsers
    public MonitoredUsers toUserEntity(SlackUserDto dto) {
        return MonitoredUsers.builder()
                .userId(dto.getId())
                .orgSaaSId(dto.getOrgSaaSId())
                .email(dto.getEmail())
                .userName(dto.getUserName())
                .timestamp(dto.getTimestamp())
                .build();
    }

    // List<SlackUserDto> -> List<MonitoredUsers>
    public List<MonitoredUsers> toUserEntity(List<SlackUserDto> dtos) {
        return dtos.stream()
                .map(this::toUserEntity)
                .collect(Collectors.toList());
    }
}
