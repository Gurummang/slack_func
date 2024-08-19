package com.GASB.slack_func.service;

import com.GASB.slack_func.mapper.SlackUserMapper;
import com.GASB.slack_func.model.dto.TopUserDTO;
import com.GASB.slack_func.model.entity.MonitoredUsers;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackUserService {

    
    private final SlackApiService slackApiService;
    private final SlackUserMapper slackUserMapper;
    private final SlackUserRepo slackUserRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final SlackChannelRepository slackChannelRepository;

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> slackFirstUsers(int workspace_config_id) {
        return CompletableFuture.runAsync(() -> {
            log.info("workspace_config_id : {}", workspace_config_id);
            List<User> slackUsers = null;
            OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspace_config_id)
                    .orElseThrow(() -> new IllegalArgumentException("OrgSaas not found with spaceId: " + workspace_config_id));
            try {
                slackUsers = slackApiService.fetchUsers(workspace_config_id).stream().filter(user -> !user.isBot()).collect(Collectors.toList());
            } catch (IOException | SlackApiException e) {
                throw new RuntimeException(e);
            }
            int orgSaaSId = orgSaaSObject.getId();
            log.info("orgSaaSId: {}", orgSaaSId);
            List<MonitoredUsers> monitoredUsers = slackUserMapper.toEntity(slackUsers, orgSaaSId);

            // 중복된 user_id를 제외하고 저장할 사용자 목록 생성
            List<MonitoredUsers> filteredUsers = monitoredUsers.stream()
                    .filter(user -> !slackUserRepo.existsByUserId(user.getUserId(),orgSaaSId))
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
            int org_saas_id = slackChannelRepository.findOrgSaaSIdByChannelId(user.getTeamId());
            MonitoredUsers monitoredUser = slackUserMapper.toEntity(user, 1);
            if (!slackUserRepo.existsByUserId(monitoredUser.getUserId(), org_saas_id)) {
                try {
                    slackUserRepo.save(monitoredUser);
                } catch (Exception e) {
                    log.error("Error saving user", e);
                    log.error("User: {}", monitoredUser.toString());
                }
            }
        });
    }


    // 쿼리문 사용할때 네이티브 쿼리면 DTO에 직접 매핑시켜줘야함
    // JPQL이면 DTO에 매핑시켜줄 필요 없음
    public List<TopUserDTO> getTopUsers(int orgId, int saasId) {
        try {
            List<Object[]> results = slackUserRepo.findTopUsers(orgId, saasId);

            return results.stream().map(result -> new TopUserDTO(
                    (String) result[0],
                    ((Number) result[1]).longValue(),
                    ((Number) result[2]).longValue(),
                    ((java.sql.Timestamp) result[3]).toLocalDateTime()
            )).collect(Collectors.toList());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving top users", e);
        }
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<TopUserDTO>> getTopUsersAsync(int orgId, int saasId) {
        return CompletableFuture.supplyAsync(() -> getTopUsers(orgId, saasId));
    }
}
