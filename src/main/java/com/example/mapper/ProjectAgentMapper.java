package com.example.mapper;

import org.springframework.stereotype.Component;
import com.example.model.dto.agent.ProjectAgentCreateDTO;
import com.example.model.dto.agent.ProjectAgentDTO;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.entity.DatabaseRole;
import com.example.model.entity.ProjectAgent;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.DatabaseRoleRepository;
import com.example.repository.ProjectDatabaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProjectAgentMapper implements BaseMapper {

    private final ProjectDatabaseRepository projectDatabaseRepository;
    private final DatabaseRoleRepository databaseRoleRepository;

    public ProjectAgentMapper(ProjectDatabaseRepository projectDatabaseRepository, DatabaseRoleRepository databaseRoleRepository) {
        this.projectDatabaseRepository = projectDatabaseRepository;
        this.databaseRoleRepository = databaseRoleRepository;
    }

    public ProjectAgent toEntityOnCreate(ProjectAgentCreateDTO dto) {
        ProjectAgent projectAgent = new ProjectAgent();
        projectAgent.setName(dto.getName());
        projectAgent.setDatabaseUsername(dto.getDatabaseUsername());
        projectAgent.setDatabasePassword(dto.getDatabasePassword());
        projectAgent.setDatabaseUrl(dto.getDatabaseUrl());
        projectAgent.setOrganizationId(dto.getOrganizationId());
        return projectAgent;
    }

    public ProjectAgentDTO toDto(ProjectAgent projectAgent) {
        ProjectAgentDTO dto = new ProjectAgentDTO();
        dto.setId(projectAgent.getId());
        dto.setName(projectAgent.getName());
        dto.setDatabaseUsername(projectAgent.getDatabaseUsername());
        dto.setDatabasePassword(projectAgent.getDatabasePassword());
        dto.setDatabaseUrl(projectAgent.getDatabaseUrl());
        return dto;
    }

    public void mapUpdate(ProjectDatabase database, List<DatabaseRoleCreateDTO> roles) {
        List<DatabaseRole> currentRoles = projectDatabaseRepository.findAllByDatabaseId(database.getId());

        List<String> agentRoles = roles
                .stream()
                .map(DatabaseRoleCreateDTO::getRoleName)
                .toList();

        List<String> databaseRoles = currentRoles
                .stream()
                .map(DatabaseRole::getName)
                .toList();

        List<DatabaseRole> newRoles = new ArrayList<>();
        if (!currentRoles.isEmpty()) {

            for (int i = 0; i < Math.max(currentRoles.size(), roles.size()); i++) {
                if (i < Math.min(currentRoles.size(), roles.size())) {
                    if (currentRoles.get(i).getName().equals(roles.get(i).getRoleName())) {
                        if (!Objects.equals(currentRoles.get(i).getDescription(),(roles.get(i).getDescription()))) {
                            currentRoles.get(i).setDescription(roles.get(i).getDescription());
                        }
                    }
                }
            }

            for (DatabaseRoleCreateDTO role : roles) {
                if (!databaseRoles.contains(role.getRoleName())) {
                    newRoles.add(new DatabaseRole(role.getRoleName(), role.getRoleName(), database.getId(), role.getDescription()));
                }
            }

            for (DatabaseRole currentRole : currentRoles) {
                if (!agentRoles.contains(currentRole.getName())) {
                    currentRole.setDeleted(true);
                }
            }
            databaseRoleRepository.saveAll(currentRoles);
        } else {
            for (DatabaseRoleCreateDTO role : roles) {
                newRoles.add(new DatabaseRole(role.getRoleName(), role.getRoleName(), database.getId(), role.getDescription()));
            }
        }

        databaseRoleRepository.saveAll(newRoles);
    }
}
