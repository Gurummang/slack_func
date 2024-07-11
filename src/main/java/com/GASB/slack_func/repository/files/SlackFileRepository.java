package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.entity.storedFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackFileRepository extends JpaRepository<storedFiles, Long> {
    Optional<storedFiles> findByFileId(String fileId);
    Optional<storedFiles> findBySaltedHash(String saltedHash);
}
