package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Column(name = "space_id")
    private String spaceId;

    @ManyToOne
    @JoinColumn(name = "config", nullable = false)
    private WorkspaceConfig config;

    @Column(name = "security_score")
    private int securityScore;

    @OneToMany(mappedBy = "orgSaas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChannelList> channels;

}
