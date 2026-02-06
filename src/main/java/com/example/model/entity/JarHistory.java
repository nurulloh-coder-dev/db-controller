package com.example.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.IdEntity;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JarHistory extends IdEntity {
    private String versionName;
    private String organizationId;
    private Instant downloadDate;
    private String downloadedBy;
}
