package com.GASB.slack_func.configuration;


import com.GASB.slack_func.annotation.SlackBoardGroup;
import com.GASB.slack_func.annotation.SlackInitGroup;
import com.GASB.slack_func.annotation.ValidEmail;
import com.GASB.slack_func.annotation.ValidWorkdSpace;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ExtractData {

    @ValidWorkdSpace(message = "Invalid Workspace ID", groups = SlackInitGroup.class)
    @NotNull(groups = SlackInitGroup.class)
    private Integer workspace_config_id;

    @ValidEmail(message = "Invalid Email", groups = SlackBoardGroup.class)
    @NotNull(groups = SlackBoardGroup.class)
    private String email;
}
