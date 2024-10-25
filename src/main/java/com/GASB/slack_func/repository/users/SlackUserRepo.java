package com.GASB.slack_func.repository.users;

import com.GASB.slack_func.model.entity.MonitoredUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackUserRepo extends JpaRepository<MonitoredUsers, Long>{
    Optional<MonitoredUsers> findByUserId(String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM MonitoredUsers u WHERE u.userId = :userId AND u.orgSaaS.id = :orgSaaSId)")
    boolean existsByUserId(@Param("userId") String userId, @Param("orgSaaSId") int orgSaaSId);




    //DISTINCT : 중복된 값을 제거하는 키워드이다.
    @Query(nativeQuery = true, value =
            "SELECT " +
                    "u.user_name AS userName, " +
                    "COUNT(DISTINCT CASE WHEN dr.info_cnt >= 1 THEN fu.id END) AS sensitiveFilesCount, " +
                    "COUNT(DISTINCT CASE WHEN vr.threat_label != 'none' THEN fu.id END) AS maliciousFilesCount, " +
                    "MAX(fu.upload_ts) AS lastUploadedTimestamp " +
                    "FROM " +
                    "    monitored_users u " +
                    "JOIN " +
                    "    org_saas os ON u.org_saas_id = os.id " +
                    "JOIN " +
                    "    file_upload fu ON u.org_saas_id = fu.org_saas_id " +
                    "JOIN " +
                    "    stored_file sf ON sf.salted_hash = fu.salted_hash " +
                    "JOIN " +
                    "    activities a ON a.user_id = u.id AND a.saas_file_id = fu.saas_file_id " +
                    "LEFT JOIN " +
                    "    dlp_report dr ON sf.id = dr.file_id " +
                    "LEFT JOIN " +
                    "    vt_report vr ON sf.id = vr.file_id " +
                    "WHERE " +
                    "    os.org_id = :orgId " +
                    "    AND os.saas_id = :saasId " +
                    "    AND (vr.threat_label != 'none' OR dr.info_cnt >= 1) " +
                    "GROUP BY " +
                    "    u.user_name " +
                    "HAVING " +
                    "    COUNT(DISTINCT CASE WHEN vr.threat_label != 'none' THEN fu.id END) > 0 " +
                    "ORDER BY " +
                    "    (5 * COUNT(DISTINCT CASE WHEN vr.threat_label != 'none' THEN fu.id END) + COUNT(DISTINCT CASE WHEN dr.info_cnt >= 1 THEN fu.id END)) DESC " +
                    "LIMIT 5")
    List<Object[]> findTopUsers(@Param("orgId") int orgId, @Param("saasId") int saasId);


    @Query("SELECT u FROM MonitoredUsers u WHERE u.userId = :user_id AND u.orgSaaS.id = :orgSaaSId")
    Optional<MonitoredUsers> findByUserIdAndOrgSaaSId(@Param("user_id") String userId, @Param("orgSaaSId") int orgSaaSId);
}
