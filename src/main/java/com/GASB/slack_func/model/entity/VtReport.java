package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class VtReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
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

    @Builder
    public VtReport(StoredFile storedFile, String type, String V3, String ALYac, String Kaspersky,
                    String Falcon, String Avast, String Sentinelone, int detectEngine,
                    int completeEngine, int score, String threatLabel, String reportUrl) {
        this.storedFile = storedFile;
        this.type = type;
        this.V3 = V3;
        this.ALYac = ALYac;
        this.Kaspersky = Kaspersky;
        this.Falcon = Falcon;
        this.Avast = Avast;
        this.Sentinelone = Sentinelone;
        this.detectEngine = detectEngine;
        this.completeEngine = completeEngine;
        this.score = score;
        this.threatLabel = threatLabel;
        this.reportUrl = reportUrl;
    }
}
