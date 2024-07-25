package com.GASB.slack_func.repository.activity;

import com.GASB.slack_func.model.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FileActivityRepo extends JpaRepository<Activities, Long>{
    Optional<Activities> findBysaasFileId(String fileId);

    Optional<Activities> findBySaasFileIdAndEventTs(String fileId, LocalDateTime eventTs);

}
