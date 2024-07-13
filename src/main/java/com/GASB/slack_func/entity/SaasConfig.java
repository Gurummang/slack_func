package com.GASB.slack_func.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "saas_config")
public class SaasConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saas_admin_email", nullable = false, length = 100)
    private String saasAdminEmail;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @Column(name = "saas_alias", nullable = false, length = 100)
    private String saasAlias;
}
