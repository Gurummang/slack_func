package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.WorkspaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceConfigRepo extends JpaRepository<WorkspaceConfig, String> {
    Optional<WorkspaceConfig> findById(int id);
    boolean existsById(int id);
}
