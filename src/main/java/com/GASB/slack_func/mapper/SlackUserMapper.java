package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.MonitoredUsers;
import com.GASB.slack_func.entity.OrgSaaS;
import com.GASB.slack_func.repository.orgSaaS.OrgSaaSRepo;
import com.slack.api.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackUserMapper {

    private final OrgSaaSRepo orgSaasRepository;

    public SlackUserMapper(OrgSaaSRepo orgSaasRepository) {
        this.orgSaasRepository = orgSaasRepository;
    }

    public MonitoredUsers toEntity(User user, int orgSaaSId) {
        if (user == null) {
            return null;
        }

        OrgSaaS orgSaaS = orgSaasRepository.findById(orgSaaSId)
                .orElseThrow(() -> new IllegalArgumentException("OrgSaas not found with id: " + orgSaaSId));

        return MonitoredUsers.builder()
                .orgSaaS(orgSaaS)
                .userId(user.getId())
                .userName(user.getRealName())
                .email(user.getProfile().getEmail())
                .build();
    }

    public List<MonitoredUsers> toEntity(List<User> users, int orgSaasId) {
        return users.stream()
                .map(user -> toEntity(user, orgSaasId))
                .collect(Collectors.toList());
    }
}
