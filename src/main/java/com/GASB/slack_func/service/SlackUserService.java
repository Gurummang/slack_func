package com.GASB.slack_func.service;

import com.GASB.slack_func.dto.SlackUserDto;
import com.GASB.slack_func.entity.MonitoredUsers;
import com.GASB.slack_func.mapper.SlackUserMapper;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SlackUserService {

    private final SlackApiService slackApiService;
    private final SlackUserMapper slackUserMapper;
    private final SlackUserRepo slackUserRepo;

    @Autowired
    public SlackUserService(SlackApiService slackApiService, SlackUserMapper slackUserMapper, SlackUserRepo slackUserRepository) {
        this.slackApiService = slackApiService;
        this.slackUserMapper = slackUserMapper;
        this.slackUserRepo = slackUserRepository;
    }

    public void slackFirstUsers() {
        try {
            List<User> slackUsers = slackApiService.fetchUsers();
            int orgSaaSId = 1; // 예시로 고정값 사용, 실제로는 orgSaaSService를 통해 가져올 수 있습니다.
            List<SlackUserDto> userDtos = slackUserMapper.toUserDto(slackUsers, orgSaaSId);
            List<MonitoredUsers> monitoredUsers = slackUserMapper.toUserEntity(userDtos);
            slackUserRepo.saveAllUsers(monitoredUsers);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error fetching users", e);
        }
    }

//    public List<MonitoredUsers> searchUser(String userName) {
//        return slackUserRepository.searchUser(userName);
//    }
}
