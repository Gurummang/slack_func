package com.GASB.slack_func.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="admin")
public class AdminUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Setter
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Setter
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Setter
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}