package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.FileUploadTable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUploadTable, Long> {
    @Query("SELECT new com.GASB.slack_func.model.dto.file.SlackRecentFileDTO(a.fileName, u.userName, sf.type, fu.timestamp) " +
            "FROM FileUploadTable fu " +
            "JOIN OrgSaaS os ON fu.orgSaaS.id = os.id " +
            "JOIN Activities a ON fu.saasFileId = a.saasFileId " +
            "JOIN StoredFile sf ON fu.hash = sf.saltedHash " +
            "JOIN MonitoredUsers u ON a.user.userId = u.userId " +
            "WHERE os.org.id = :orgId AND os.saas.id = :saasId " +
            "ORDER BY fu.timestamp DESC LIMIT 10")
    List<SlackRecentFileDTO> findRecentFilesByOrgIdAndSaasId(@Param("orgId") int orgId, @Param("saasId") int saasId);

    // Corrected method to find by OrgSaaS fields

    Optional<FileUploadTable> findBySaasFileIdAndTimestamp(String saasFileId, LocalDateTime timestamp);

    @Transactional
    @Modifying
    @Query("UPDATE FileUploadTable fu " +
            "SET fu.deleted = true " +
            "WHERE fu.saasFileId = :saasFileId")
    void checkDelete(@Param("saasFileId") String saasFileId);



    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM FileUploadTable f " +
            "JOIN OrgSaaS os ON f.orgSaaS.id = os.id " +
            "JOIN AdminUsers a ON os.org.id = a.org.id " +
            "WHERE a.email = :email " +
            "AND os.saas.id = :saasId " +
            "AND f.hash = :saltedHash")
    boolean existsByUserAndHash(@Param("email") String email,
                                @Param("saasId") int saasId,
                                @Param("saltedHash") String saltedHash);
}
