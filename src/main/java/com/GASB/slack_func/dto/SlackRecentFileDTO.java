package com.GASB.slack_func.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SlackRecentFileDTO {
    private String fileName;
    private String uploadedBy;
    private String fileType;
    private LocalDateTime uploadTimestamp;
}
