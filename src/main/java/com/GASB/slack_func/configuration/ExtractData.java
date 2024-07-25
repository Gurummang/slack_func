package com.GASB.slack_func.configuration;


import com.GASB.slack_func.annotation.ValidEmail;
import com.GASB.slack_func.annotation.ValidSpaceId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExtractData {

    @ValidSpaceId(message = "Invalid Space ID")
    private String spaceId;

    @ValidEmail(message = "Invalid Email")
    private String email;
}
