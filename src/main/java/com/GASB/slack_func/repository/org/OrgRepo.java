package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.Org;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgRepo extends JpaRepository<Org, Integer> {

}
