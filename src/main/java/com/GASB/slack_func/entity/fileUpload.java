package com.GASB.slack_func.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class fileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int orgSaaSId;
    private String SaasFileId;
    private String hash;
    private LocalDateTime timestamp;
}
