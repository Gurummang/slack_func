package com.GASB.slack_func.controller;

import com.GASB.slack_func.service.SlackFileService;
import com.slack.api.methods.SlackApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class SlackFileController {

    @Autowired
    private SlackFileService slackFileService;

    @GetMapping("/listFiles")
    public Map<String, Object> listFiles(@RequestParam int count, @RequestParam int page) {
        try {
            return slackFileService.listFiles(count, page);
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
            // 필요하다면 적절한 응답을 반환하도록 설정
            return Map.of("ok", false, "message", "error: " + e.getMessage());
        }
    }
}
