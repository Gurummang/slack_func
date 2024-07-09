package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "idx_file_id", columnList = "SaasFileId", unique = true))
public class fileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int orgSaaSId;
    private String SaasFileId;
    private String hash;
    private LocalDateTime timestamp;
}
