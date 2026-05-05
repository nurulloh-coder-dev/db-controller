package com.example.model.dto.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.model.dto.agent.ProjectAgentDTO;
import com.example.model.dto.databaseUser.ProjectDatabaseUserDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDatabaseDto {
    private String id;
    private String name;
    private ProjectAgentDTO agent;

    private List<ProjectDatabaseUserDto> members;
}
