package com.example.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.BaseEntity;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProjectAgent extends BaseEntity {
    private String name;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private String organizationId;
}
