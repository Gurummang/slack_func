package com.GASB.slack_func.configuration;

import com.slack.api.model.Conversation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class ServiceConfig {
    @Bean
    public ConcurrentMap<String, Conversation> concurrentMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
