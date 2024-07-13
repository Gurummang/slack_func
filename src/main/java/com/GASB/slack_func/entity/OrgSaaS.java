package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "org_saas")
public class OrgSaaS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @ManyToOne
    @JoinColumn(name = "saas_id", nullable = false)
    private Saas saas;

    @Column(name = "status", nullable = false)
    private int status;

    @ManyToOne
    @JoinColumn(name = "config_file", nullable = false)
    private SaasConfig configFile;

    @Column(name = "security_score")
    private int securityScore;
}
