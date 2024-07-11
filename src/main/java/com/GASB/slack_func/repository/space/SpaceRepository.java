package com.GASB.slack_func.repository.space;

import com.GASB.slack_func.entity.SpaceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<SpaceList, Long> {
}
