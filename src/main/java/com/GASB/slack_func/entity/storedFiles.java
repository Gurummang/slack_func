package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "stored_file")
public class storedFiles {
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

    @Builder
    public storedFiles(long id, String fileId, String saltedHash, int size, String type, String savePath){
        this.id = id;
//        this.fileId = fileId;
        this.saltedHash = saltedHash;
        this.size = size;
        this.type = type;
        this.savePath = savePath;
    }
}
