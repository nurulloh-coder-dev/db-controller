package com.example.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.IdEntity;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlan extends IdEntity {
    private String name;
    private String description;
    private BigDecimal price;

    private Integer maxUsers;
    private Integer maxDatabases;
    private Integer maxMembersPerDatabase;

    private List<String> features;
    private Boolean active = true;
}