package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VtReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private StoredFile storedFile;

    private String type;
    private String V3;
    private String ALYac;
    private String Kaspersky;
    private String Falcon;
    private String Avast;
    private String Sentinelone;

    private int detectEngine;
    private int completeEngine;
    private int score;
    private String threatLabel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reportUrl;
}
