//package com.GASB.slack_func.service.SlackOAuth;
//
//import com.GASB.slack_func.configuration.SlackConfig;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@Slf4j
//public class SlackOAuthService {
//
//    @Autowired
//    private SlackConfig slackConfig;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    public Map<String, String> getAccessToken(String code) {
//        String url = "https://slack.com/api/oauth.v2.access";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        Map<String, String> params = new HashMap<>();
//        params.put("client_id", slackConfig.getClientId());
//        params.put("client_secret", slackConfig.getClientSecret());
//        params.put("code", code);
//        params.put("redirect_uri", slackConfig.getRedirectUri());
//
//        StringBuilder requestBody = new StringBuilder();
//        params.forEach((key, value) -> {
//            if (requestBody.length() > 0) {
//                requestBody.append("&");
//            }
//            requestBody.append(key).append("=").append(value);
//        });
//
//        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
//
//        log.info("Requesting access token with params: {}", requestBody); // 요청 매개변수 확인
//        return response.getBody();
//    }
//
//    public String getClientId() {
//        return slackConfig.getClientId();
//    }
//
//    public String getClientSecret() {
//        return slackConfig.getClientSecret();
//    }
//
//    public String getRedirectUri() {
//        return slackConfig.getRedirectUri();
//    }
//}
