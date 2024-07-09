package com.GASB.slack_func.repository;

import com.GASB.slack_func.entity.storedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlackFileRepository extends JpaRepository<storedFiles, Long> {
    Optional<storedFiles> findByFileId(String fileId);
}
