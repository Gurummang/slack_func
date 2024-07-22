package com.GASB.slack_func.repository.AV;

import com.GASB.slack_func.model.entity.StoredFile;
import com.GASB.slack_func.model.entity.VtReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VtReportRepository extends JpaRepository<VtReport, Long> {
    Optional<VtReport> findByStoredFile(StoredFile storedFile);
}
