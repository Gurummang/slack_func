package com.GASB.slack_func.repository;

import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileUploadRepository extends JpaRepository<fileUpload,Long> {
    Optional<fileUpload> findBySaaSFileId(String SaasFileId);
}
