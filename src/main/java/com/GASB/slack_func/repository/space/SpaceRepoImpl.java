package com.GASB.slack_func.repository.space;

import com.GASB.slack_func.dto.SpaceDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class SpaceRepoImpl implements SpaceCustomRepo{


    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional
    public void saveSpaceInfo(SpaceDto teamDto, int saasId) {

    }
}
