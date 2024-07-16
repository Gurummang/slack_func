package com.GASB.slack_func.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlackFileSharedEventDto {
    private String from;
    private String event;
    private String saas;
    private String fileId;
}
