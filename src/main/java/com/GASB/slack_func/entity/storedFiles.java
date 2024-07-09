package com.GASB.slack_func.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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
