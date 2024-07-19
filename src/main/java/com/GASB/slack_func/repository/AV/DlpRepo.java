package com.GASB.slack_func.repository.AV;

import com.GASB.slack_func.model.entity.DlpReport;
import com.GASB.slack_func.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DlpRepo extends JpaRepository<DlpReport,Long> {
    Optional<DlpReport> findByStoredFile(StoredFile storedFile);
}
