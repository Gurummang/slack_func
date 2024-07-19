package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "stored_file")
public class StoredFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    private String fileId;

    @Column(columnDefinition = "TEXT", nullable = false) //원래는 false가 맞음
    private String saltedHash;

    private int size;
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String savePath;

}
