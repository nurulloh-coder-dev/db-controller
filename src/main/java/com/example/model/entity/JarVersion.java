package com.example.model.entity;


import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import com.example.model.entity.base.BaseEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
public class JarVersion extends BaseEntity {
    private String version;
    private Long sizeInBytes;
    private String description;
    private Instant releaseDate;
}
