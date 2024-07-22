package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "file_upload")
public class fileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false, referencedColumnName = "id")
    private OrgSaaS orgSaaS;

    @Column(name = "saas_file_id", nullable = false, unique = true)
    private String saasFileId;

    @Column(nullable = false, name="salted_hash")
    private String hash;

    @Column(name = "upload_ts", nullable = false)
    private Timestamp timestamp;
}
