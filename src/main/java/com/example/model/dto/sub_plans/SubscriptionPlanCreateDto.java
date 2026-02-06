package com.example.model.dto.sub_plans;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionPlanCreateDto {
    private String name;        // "Pro Plan", "Enterprise Plan"
    private String description; // "Perfect for growing teams"

    private BigDecimal price;   // 29.99, 99.99, 199.99

    private Integer maxUsers;       // 10, 50, unlimited (-1)
    private Integer maxDatabases;
    private Integer maxMemberPerDatabase;
}
