package com.example.model.dto.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDatabaseCreateDto {
    private String name;
    private String description;
    private String agentId;
    private String organizationId;
    private List<Long> membersId;
    private Integer version;
}
