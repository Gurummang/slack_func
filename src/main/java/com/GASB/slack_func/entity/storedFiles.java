package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "idx_file_id", columnList = "fileId", unique = true))
public class storedFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String fileId; // 필드명 수정: fildId -> fileId
    private String hash;
    private int size;
    private String type;
    private String fileName; // 필드명 수정: file_name -> fileName
    private String savePath; // 필드명 수정: save_path -> savePath
}
