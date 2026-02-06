package com.example.controller.active_users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {

    @Modifying
    @Query(
            value = "UPDATE user_session SET last_activity_time = :time WHERE user_id IN (:sessionsId)",
            nativeQuery = true
    )
    void updateUserLastActivity(
            @Param("time") Instant time,
            @Param("sessionsId") Collection<String> sessionId
    );

    @Query(value = """
    SELECT 
        us.user_id AS userId,
        u.username AS userName,
        u.email AS email,
        o.id AS organizationId,
        o.organization_name AS organizationName,
        us.login_time AS sessionStart,
        us.last_activity_time AS lastActivity
    FROM user_session us
    INNER JOIN auth_user u ON us.user_id = u.id
    INNER JOIN organization o ON u.organization_id = o.id
    WHERE us.login_time >= :date
    """, nativeQuery = true)
    List<ActiveUserProjection> getActiveUsersByGivenDate(@Param("date") Instant startOfDay);

    @Query(value = "select count(distinct user_id) from user_session where last_activity_time >= :threshold", nativeQuery = true)
    Integer countAllCurrentlyActives(@Param("threshold") Instant threshold);
}
