package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.entity.MonitoredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlackUserRepo extends JpaRepository<MonitoredUsers, Long> , SlackUserIF{

}
