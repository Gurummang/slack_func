package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.dto.SlackUserDto;
import com.GASB.slack_func.entity.MonitoredUsers;

import java.util.List;

public interface SlackUserIF {
    void saveAllUsers(List<MonitoredUsers> users);
//    List<MonitoredUsers> searchUser(String userName);
}
