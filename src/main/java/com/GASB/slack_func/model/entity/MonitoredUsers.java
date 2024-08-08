package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "monitored_users")
public class MonitoredUsers {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int id;

    @Id
    @Column(name = "user_id", unique = true, length = 100)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false,referencedColumnName = "id")
    private OrgSaaS orgSaaS;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "status")
    private Timestamp timestamp;
    //이것도 타임스탬프로 해야하나

    @Builder
    public MonitoredUsers(String userId, OrgSaaS orgSaaS, String email, String userName, Timestamp timestamp) {
        this.userId = userId;
        this.orgSaaS = orgSaaS;
        this.email = email;
        this.userName = userName;
        this.timestamp = timestamp;
    }
}
