package com.GASB.slack_func.repository.AV;

import com.GASB.slack_func.model.entity.FileStatus;
import com.GASB.slack_func.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileStatusRepository extends JpaRepository<FileStatus, Long>{
    FileStatus findByStoredFile(StoredFile storedFile);
}