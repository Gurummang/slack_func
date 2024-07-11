package com.GASB.slack_func.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceDto {
    private int saasId;
    private String spaceId;
    private String spaceName;
    private String spaceUrl;
}
