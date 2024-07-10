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
public class SpaceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spaceId;
    private String spaceName;

    @Builder
    public SpaceList(String spaceId, String spaceName) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
    }
}
