package com.GASB.slack_func.repository;

import com.GASB.slack_func.entity.storedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackFileRepository extends JpaRepository<storedFiles, Long> {
}
