package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.model.entity.Saas;
import com.GASB.slack_func.model.entity.fileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories(basePackages = "com.GASB.slack_func.repository.files")
public interface FileUploadRepository extends JpaRepository<fileUpload, Long> {
    Optional<fileUpload> findBySaasFileId(String saasFileId);
    List<fileUpload> findTop10ByOrderByTimestampDesc();
    List<fileUpload> findByOrgSaaS(OrgSaaS orgSaaS);
    List<fileUpload> findByOrgSaaSInOrderByTimestampDesc(List<OrgSaaS> orgSaaSList);

    // Corrected method to find by OrgSaaS fields

    Optional<fileUpload> findBySaasFileIdAndTimestamp(String saasFileId, LocalDateTime timestamp);
    List<fileUpload> findTop10ByOrgSaaS_Org_IdAndOrgSaaS_SaasOrderByTimestampDesc(int orgId, Saas saas);
}
