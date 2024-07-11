package com.GASB.slack_func.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlackUserDto {
    private String id;
    private int orgSaaSId;
    private String email;
    private String userName;
    private Long timestamp;
}
