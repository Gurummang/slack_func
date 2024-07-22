package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.model.entity.MonitoredUsers;

import java.util.List;

public interface SlackUserIF {
    void saveAllUsers(List<MonitoredUsers> users);
}
