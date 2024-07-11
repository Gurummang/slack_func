package com.GASB.slack_func.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MonitoredUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private int orgSaaSId;
    private String email;
    private String userName;
    private Long timestamp;

    @Builder
    public MonitoredUsers(String userId, int orgSaaSId, String email, String userName, Long timestamp) {
        this.userId = userId;
        this.orgSaaSId = orgSaaSId;
        this.email = email;
        this.userName = userName;
        this.timestamp = timestamp;
    }
}
