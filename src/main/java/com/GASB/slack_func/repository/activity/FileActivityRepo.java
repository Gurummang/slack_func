package com.GASB.slack_func.repository.activity;

import com.GASB.slack_func.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileActivityRepo extends JpaRepository<Activities, Long>{
    Optional<Activities> findBysaasFileId(String fileId);
}
