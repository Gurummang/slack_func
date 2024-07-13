package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.entity.MonitoredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackUserRepo extends JpaRepository<MonitoredUsers, Long> , SlackUserIF{
    Optional<MonitoredUsers> findByUserId(String userId);
    String findUserNameByUserId(String userId);

    boolean existsByUserId(String userId);
}
