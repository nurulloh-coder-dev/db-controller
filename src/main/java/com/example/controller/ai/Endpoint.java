package com.example.controller.ai;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import com.example.model.entity.base.IdEntity;

import java.util.List;

@Entity
@Setter
@Getter
public class Endpoint extends IdEntity {
    private String path;
    private String method;
    private Integer totalReqPerDay;
    private Integer avgResponseTime;
    private Double successRate;
    private Double failureRate;
    private List<String> peakHours;
}
