package com.GASB.slack_func.service.SlackOAuth;

import com.GASB.slack_func.configuration.SlackConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackOAuthService {

    @Autowired
    private SlackConfig slackConfig;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, String> getAccessToken(String code) {
        String url = "https://slack.com/api/oauth.v2.access";

        Map<String, String> params = new HashMap<>();
        params.put("client_id", slackConfig.getClientId());
        params.put("client_secret", slackConfig.getClientSecret());
        params.put("code", code);
        params.put("redirect_uri", slackConfig.getRedirectUri());

        return restTemplate.postForObject(url, params, Map.class);
    }

    public String getClientId() {
        return slackConfig.getClientId();
    }

    public String getRedirectUri() {
        return slackConfig.getRedirectUri();
    }
}