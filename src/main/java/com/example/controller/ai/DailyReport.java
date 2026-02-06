package com.example.controller.ai;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import com.example.model.entity.base.IdEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class DailyReport extends IdEntity {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endpoint> endpoints;
    private LocalDate date;
}
