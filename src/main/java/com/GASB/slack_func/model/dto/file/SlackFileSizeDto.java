package com.GASB.slack_func.model.dto.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackFileSizeDto {
    private int totalSize;
    private int sensitiveSize;
    private int maliciousSize;
}
