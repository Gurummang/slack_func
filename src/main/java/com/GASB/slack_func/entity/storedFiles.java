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
    private String fileId;
    private String hash;
    private int size;
    private String type;
    private String fileName;
    private String savePath;
}
