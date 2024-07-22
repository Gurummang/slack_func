package com.GASB.slack_func.repository.files;

import com.GASB.slack_func.model.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackFileRepository extends JpaRepository<StoredFile, Long> {
    Optional<StoredFile> findBySaltedHash(String saltedHash);
}
