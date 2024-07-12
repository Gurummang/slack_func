package com.GASB.slack_func.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SlackRecentFileDTO {
    private String fileId;
    private String fileName;
    private String uploadedBy;
    private String fileType;
    private LocalDateTime uploadTimestamp;
}
