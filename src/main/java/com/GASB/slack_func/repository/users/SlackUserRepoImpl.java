package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.model.entity.MonitoredUsers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

public class SlackUserRepoImpl implements SlackUserIF{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void saveAllUsers(List<MonitoredUsers> users) {
        users.forEach(entityManager::persist);
    }

    // Other methods

}
