package com.GASB.slack_func.annotation.JWT;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Aspect
@Component
@RequiredArgsConstructor
public class JWTValidationAspect {

    private static final Logger log = LoggerFactory.getLogger(JWTValidationAspect.class);

    private final HttpServletRequest servletRequest;
    private final RestTemplate restTemplate;

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Before("@annotation(com.GASB.slack_func.annotation.JWT.ValidateJWT)")
    public void validateJWT() {
        // 쿠키값 추출
        Cookie[] cookies = servletRequest.getCookies();
        String jwtToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken == null) {
            throw new IllegalArgumentException("JWT token not found in cookies");
        }

        // 인증 서버로 JWT 검증 요청
        String email = validateRequest(jwtToken);

        servletRequest.setAttribute("email", email);
    }

    private String validateRequest(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            log.info("Sending request to auth server with token: {}", jwtToken);
            log.info("request content: {}", entity);
            ResponseEntity<JwtValidationResponse> response = restTemplate.postForEntity(authServerUrl, entity, JwtValidationResponse.class);
            log.info("Received response from auth server: {}", response);

            JwtValidationResponse jwtValidationResponse = response.getBody();
            log.info("JWT validation response: {}", jwtValidationResponse);
            if (jwtValidationResponse == null || !"OK".equals(jwtValidationResponse.getStatus())) {
                throw new IllegalArgumentException("JWT token is invalid or the validation response is null");
            }
            return jwtValidationResponse.getEmail();
        } catch (HttpClientErrorException e) {
            log.error("JWT token validation failed: {} - Response body: {}", e.getMessage(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("JWT token validation failed: " + e.getMessage(), e);
        }
    }
}
