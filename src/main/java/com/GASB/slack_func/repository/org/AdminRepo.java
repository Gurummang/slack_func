package com.GASB.slack_func.repository.org;

import com.GASB.slack_func.model.entity.AdminUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepo extends JpaRepository<AdminUsers, Integer> {


    Optional<AdminUsers> findByEmail(String email);
    Optional<AdminUsers> findByEmailAndPassword(String email, String password);
    Optional<AdminUsers> findByEmailAndOrgId(String email, int orgId);

}
