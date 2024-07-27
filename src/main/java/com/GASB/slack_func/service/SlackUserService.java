package com.GASB.slack_func.service;

import com.GASB.slack_func.mapper.SlackUserMapper;
import com.GASB.slack_func.model.entity.MonitoredUsers;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackUserService {

    private final SlackApiService slackApiService;
    private final SlackUserMapper slackUserMapper;
    private final SlackUserRepo slackUserRepo;
    private final OrgSaaSRepo orgSaaSRepo;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> slackFirstUsers(int workspace_config_id) {
        return CompletableFuture.runAsync(() -> {
            log.info("workspace_config_id : {}", workspace_config_id);
            List<User> slackUsers = null;
            OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspace_config_id)
                    .orElseThrow(() -> new IllegalArgumentException("OrgSaas not found with spaceId: " + workspace_config_id));
            try {
                slackUsers = slackApiService.fetchUsers(workspace_config_id);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SlackApiException e) {
                throw new RuntimeException(e);
            }
            int orgSaaSId = orgSaaSObject.getId();
            log.info("orgSaaSId: {}", orgSaaSId);
            List<MonitoredUsers> monitoredUsers = slackUserMapper.toEntity(slackUsers, orgSaaSId);

            // 중복된 user_id를 제외하고 저장할 사용자 목록 생성
            List<MonitoredUsers> filteredUsers = monitoredUsers.stream()
                    .filter(user -> !slackUserRepo.existsByUserId(user.getUserId()))
                    .collect(Collectors.toList());

            slackUserRepo.saveAll(filteredUsers);
        }).exceptionally(ex -> {
            log.error("Error fetching users", ex);
            throw new RuntimeException(ex);
        });
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> addUser(User user) {
        return CompletableFuture.runAsync(() -> {
            MonitoredUsers monitoredUser = slackUserMapper.toEntity(user, 1);
            if (!slackUserRepo.existsByUserId(monitoredUser.getUserId())) {
                slackUserRepo.save(monitoredUser);
            }
        });
    }
}
