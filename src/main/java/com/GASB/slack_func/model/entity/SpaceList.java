package com.GASB.slack_func.model.entity;

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
    private int saasId;
    private String spaceId;
    private String spaceName;
    private String spaceUrl;

    @Builder
    public SpaceList(int saasId, String spaceId, String spaceName, String spaceUrl) {
        this.saasId = saasId;
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.spaceUrl = spaceUrl;
    }
}
