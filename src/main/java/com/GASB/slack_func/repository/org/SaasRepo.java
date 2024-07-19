package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.Saas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaasRepo extends JpaRepository<Saas,Long> {
    Optional<Saas> findById(int saas_id);
}
