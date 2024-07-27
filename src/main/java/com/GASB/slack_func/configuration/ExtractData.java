package com.GASB.slack_func.configuration;


import com.GASB.slack_func.annotation.ValidEmail;
import com.GASB.slack_func.annotation.ValidWorkdSpace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExtractData {

//    @ValidSpaceId(message = "Invalid Space ID")
//    private String spaceId;

    @ValidWorkdSpace(message = "Invalid Workspace ID")
    private int workspace_config_id;

    @ValidEmail(message = "Invalid Email")
    private String email;
}
