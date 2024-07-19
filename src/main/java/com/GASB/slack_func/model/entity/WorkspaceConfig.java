package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workspace_config")
public class WorkspaceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saas_admin_email", nullable = false, length = 100)
    private String saasAdminEmail;

    @Column(name = "saas_name", nullable = false, length = 100)
    private String saasname;

    @Column(name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrgSaaS> orgSaaSList;
}
