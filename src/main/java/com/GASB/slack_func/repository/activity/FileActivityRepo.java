package com.GASB.slack_func.repository.activity;

import com.GASB.slack_func.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FileActivityRepo extends JpaRepository<Activities, Long>{
    Optional<Activities> findBysaasFileId(String fileId);

    Optional<Activities> findBySaasFileIdAndEventTs(String fileId, LocalDateTime eventTs);

    @Query("SELECT a FROM Activities a WHERE a.eventTs = :eventTs AND a.eventType = :eventType")
    Optional<Activities> findByEventTsAndEventType(@Param("eventTs") LocalDateTime eventTs, @Param("eventType") String eventType);



    @Query("SELECT a.user.userId FROM Activities a WHERE a.saasFileId = :fileId AND a.eventType = 'file_upload'")
    Optional<String> findUserBySaasFileId(@Param("fileId") String fileId);


    @Query("SELECT a.fileName FROM Activities a WHERE a.saasFileId = :fileId")
    Optional<String> findFileNamesBySaasFileId(@Param("fileId")String fileId);
}
