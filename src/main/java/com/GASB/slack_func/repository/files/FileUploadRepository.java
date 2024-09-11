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
            "JOIN MonitoredUsers u ON a.user.id = u.id " +
            "WHERE os.org.id = :orgId AND os.saas.id = :saasId " +
            "AND a.eventType = 'file_upload' " +  // 조건 추가
            "ORDER BY fu.timestamp DESC LIMIT 10")
    List<SlackRecentFileDTO> findRecentFilesByOrgIdAndSaasId(@Param("orgId") int orgId, @Param("saasId") int saasId);



    @Query("SELECT f FROM FileUploadTable f WHERE f.timestamp = :timestamp AND f.hash = :hash")
    Optional<FileUploadTable> findByTimestampAndHash(@Param("timestamp") LocalDateTime timestamp, @Param("hash") String hash);

    @Transactional
    @Modifying
    @Query("UPDATE FileUploadTable fu " +
            "SET fu.deleted = true " +
            "WHERE fu.saasFileId = :saasFileId AND fu.id IS NOT NULL")
    void checkDelete(@Param("saasFileId") String saasFileId);


    @Query("SELECt fu FROM FileUploadTable fu WHERE fu.id = :idx")
    Optional<FileUploadTable> findSaasfileIdByid(@Param("idx")int idx);

    @Query("SELECT fu FROM FileUploadTable fu WHERE fu.hash = :file_hash")
    Optional<FileUploadTable> findByFileHash(@Param("file_hash")String file_hash);

    @Query("SELECT fu FROM FileUploadTable fu WHERE fu.hash = :file_hash AND fu.id = :idx")
    Optional<FileUploadTable> findByIdAndFileHash(@Param("idx") int idx, @Param("file_hash")String file_hash);


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
