package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.model.entity.Saas;
import com.GASB.slack_func.model.entity.fileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<fileUpload, Long> {
    @Query("SELECT new com.GASB.slack_func.model.dto.file.SlackRecentFileDTO(a.fileName, u.userName, sf.type, fu.timestamp) " +
            "FROM fileUpload fu " +
            "JOIN OrgSaaS os ON fu.orgSaaS.id = os.id " +
            "JOIN Activities a ON fu.saasFileId = a.saasFileId " +
            "JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "JOIN MonitoredUsers u ON a.user.userId = u.userId " +
            "WHERE os.org.id = :orgId AND os.saas.id = :saasId " +
            "ORDER BY fu.timestamp DESC LIMIT 10")
    List<SlackRecentFileDTO> findRecentFilesByOrgIdAndSaasId(@Param("orgId") int orgId, @Param("saasId") int saasId);
    List<fileUpload> findTop10ByOrgSaaSInOrderByTimestampDesc(List<OrgSaaS> orgSaasList);


    // Corrected method to find by OrgSaaS fields

    Optional<fileUpload> findBySaasFileIdAndTimestamp(String saasFileId, LocalDateTime timestamp);
    List<fileUpload> findTop10ByOrgSaaS_Org_IdAndOrgSaaS_SaasOrderByTimestampDesc(int orgId, Saas saas);
}
