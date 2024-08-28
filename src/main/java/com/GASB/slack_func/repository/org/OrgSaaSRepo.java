package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.model.entity.Saas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrgSaaSRepo extends JpaRepository<OrgSaaS, Integer> {
//    OrgSaaS findByOrgId(String orgId);

    Optional<OrgSaaS> findById(Long id);

    @Query("SELECT os FROM OrgSaaS os WHERE os.spaceId = :spaceId")
    Optional<OrgSaaS> findBySpaceIdUsingQuery(@Param("spaceId") String spaceId);
    Optional<OrgSaaS> findBySpaceId(String spaceId);
    Optional<OrgSaaS> findByOrgIdAndSpaceId(int orgId, String spaceId);
    Optional<OrgSaaS> findByOrgIdAndSaas(int orgId, Saas saas);
    List<OrgSaaS> findAllByOrgIdAndSaas(int orgId, Saas saas);
    Optional<OrgSaaS> findBySpaceIdAndOrgId(String spaceId, int orgId);


    boolean existsBySpaceId(String spaceId);
}
