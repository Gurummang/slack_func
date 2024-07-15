package com.GASB.slack_func.repository.orgSaaS;

import com.GASB.slack_func.model.entity.OrgSaaS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Integer> {

    Optional<OrgSaaS> findById(Long id);

}
