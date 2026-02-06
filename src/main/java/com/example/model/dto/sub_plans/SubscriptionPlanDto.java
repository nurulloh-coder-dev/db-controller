package com.example.model.dto.sub_plans;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionPlanDto {
    private String id;
    private String name;

    private String description;

    private BigDecimal price;

    private Integer maxUsers;
    private Integer maxDatabases;
    private Integer maxMembersPerDatabase;
}
