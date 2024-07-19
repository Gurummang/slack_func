package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FileStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private StoredFile storedFile;

    @Builder.Default
    private int GscanStatus = -1;
    @Builder.Default
    private int DlpStatus = -1;
    @Builder.Default
    private int VtStatus = -1;

}