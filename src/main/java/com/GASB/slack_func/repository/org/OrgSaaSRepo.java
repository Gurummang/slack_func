package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.model.entity.Saas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Integer> {
//    OrgSaaS findByOrgId(String orgId);

    Optional<OrgSaaS> findById(Long id);
    Optional<OrgSaaS> findBySpaceId(String spaceId);
//
//    Optional<OrgSaaS> findByOrgId(int orgId);
//    Optional<OrgSaaS> findByOrgIdAndsaaSId(int orgId, int saasId);
    Optional<OrgSaaS> findByOrgIdAndSaas(int orgId, Saas saas);
    Optional<OrgSaaS> findBySpaceIdAndOrgId(String spaceId, int orgId);
}
