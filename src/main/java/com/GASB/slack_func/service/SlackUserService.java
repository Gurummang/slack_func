package com.GASB.slack_func.service;

import com.GASB.slack_func.mapper.SlackUserMapper;
import com.GASB.slack_func.model.entity.MonitoredUsers;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    public CompletableFuture<Void> slackFirstUsers(String spaceId, int orgId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("SpaceId : {}", spaceId);
                OrgSaaS orgSaaSObject = orgSaaSRepo.findByOrgIdAndSpaceId(orgId, spaceId).orElseThrow(() -> new RuntimeException("OrgSaaS not found"));
                List<User> slackUsers = slackApiService.fetchUsers(orgSaaSObject);
                int orgSaaSId = orgSaaSObject.getId().intValue(); // 예시로 고정값 사용, 실제로는 orgSaaSService를 통해 가져올 수 있습니다.
                log.info("orgSaaSId: {}", orgSaaSId);
                List<MonitoredUsers> monitoredUsers = slackUserMapper.toEntity(slackUsers, orgSaaSId);

                // 중복된 user_id를 제외하고 저장할 사용자 목록 생성
                List<MonitoredUsers> filteredUsers = monitoredUsers.stream()
                        .filter(user -> !slackUserRepo.existsByUserId(user.getUserId()))
                        .collect(Collectors.toList());

                slackUserRepo.saveAll(filteredUsers);
            } catch (Exception e) {
                log.error("Error fetching users", e);
            }
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
