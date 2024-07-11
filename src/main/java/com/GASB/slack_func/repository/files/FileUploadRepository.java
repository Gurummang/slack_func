package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.entity.fileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<fileUpload,Long> {
    Optional<fileUpload> findBySaasFileId(String saasFileId);
}
