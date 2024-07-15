package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_saas_file_id", columnList = "saasFileId", unique = true), name = "file_upload")
public class fileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int orgSaaSId;
    private String saasFileId;
    private String hash;
    private int timestamp;

    @Builder
    public fileUpload(int orgSaaSId, String saasFileId, String hash, int timestamp) {
        this.orgSaaSId = orgSaaSId;
        this.saasFileId = saasFileId;
        this.hash = hash;
        this.timestamp = timestamp;
    }
}
