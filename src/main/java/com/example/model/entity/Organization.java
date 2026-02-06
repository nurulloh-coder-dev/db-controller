package com.example.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.BaseEntity;
import com.example.model.entity.enums.SubscriptionStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Organization extends BaseEntity {
    private String organizationName;

    @ManyToOne
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan currentPlan;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    private LocalDateTime trialEndDate;           // 14 days from creation
    private LocalDateTime subscriptionEndDate;    // Next billing date
}
