package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "activities")
public class Activities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "saas_file_id")
    private String saasFileId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "event_ts")
    private LocalDateTime eventTs;

    @Column(name = "upload_channel")
    private String uploadChannel;

    @Builder
    public Activities(String user, String eventType, String saasFileId, String fileName, LocalDateTime eventTs, String uploadChannel) {
        this.userId = user;
        this.eventType = eventType;
        this.saasFileId = saasFileId;
        this.fileName = fileName;
        this.eventTs = eventTs;
        this.uploadChannel = uploadChannel;
    }
}
