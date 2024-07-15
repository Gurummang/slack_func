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
public class FileStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private StoredFile storedFile;

    private int GscanStatus = -1;
    private int DlpStatus = -1;
    private int VtStatus = -1;

    @Builder
    public FileStatus(StoredFile storedFile, Integer GscanStatus, Integer DlpStatus, Integer VtStatus) {
        this.storedFile = storedFile;
        this.GscanStatus = (GscanStatus != null) ? GscanStatus : -1;
        this.DlpStatus = (DlpStatus != null) ? DlpStatus : -1;
        this.VtStatus = (VtStatus != null) ? VtStatus : -1;
    }

}