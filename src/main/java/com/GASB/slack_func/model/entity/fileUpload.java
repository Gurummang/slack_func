package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_saas_file_id", columnList = "saasFileId", unique = true), name = "file_upload")
public class fileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false)
    private OrgSaaS orgSaaS;

    @Column(name = "saas_file_id", nullable = false, unique = true)
    private String saasFileId;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private int timestamp;
}
