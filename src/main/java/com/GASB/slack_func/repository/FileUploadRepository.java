package com.GASB.slack_func.repository;

import com.GASB.slack_func.entity.fileUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<fileUpload,Long> {
}
