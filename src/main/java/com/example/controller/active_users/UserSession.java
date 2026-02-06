package com.example.controller.active_users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import com.example.model.entity.base.IdEntity;

import java.time.Instant;

@Entity
@Getter
@Setter
public class UserSession extends IdEntity {
    private String userId;
    @Column(name = "login_time", columnDefinition = "TIMESTAMP")
    private Instant loginTime;

    @Column(name = "last_activity_time", columnDefinition = "TIMESTAMP")
    private Instant lastActivityTime;

//    private Boolean active = true;
}
