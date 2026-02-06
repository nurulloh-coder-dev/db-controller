package com.example.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.database.ProjectDatabaseCreateDto;
import com.example.model.dto.database.ProjectDatabaseDto;
import com.example.model.dto.database.ProjectDatabaseUpdateDto;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectDatabaseRepository;
import com.example.validator.OrganizationValidator;
import com.example.validator.ProjectAgentValidator;

import java.util.List;

@Component
@AllArgsConstructor
public class ProjectDatabaseMapper implements BaseMapper {
    private final ProjectDatabaseRepository repository;
    private final ProjectDatabaseUserMapper databaseUserMapper;
    private final ProjectAgentValidator projectAgentValidator;
    private final ProjectAgentMapper projectAgentMapper;
    private final OrganizationValidator organizationValidator;

    public ProjectDatabase toEntityFromCreate(ProjectDatabaseCreateDto createDto) {
        ProjectDatabase projectDatabase = new ProjectDatabase();
        projectDatabase.setName(createDto.getName());
        projectDatabase.setDescription(createDto.getDescription());
        projectDatabase.setOrganization(organizationValidator.validateId(createDto.getOrganizationId()));
        projectDatabase.setAgent(projectAgentValidator.existsAndGet(createDto.getAgentId()));
        return projectDatabase;
    }

    public ProjectDatabaseDto toDto(ProjectDatabase save) {
        ProjectDatabaseDto projectDatabaseDto = new ProjectDatabaseDto();
        projectDatabaseDto.setId(save.getId());
        projectDatabaseDto.setName(save.getName());

        projectDatabaseDto.setDescription(save.getDescription());
        projectDatabaseDto.setAgent(projectAgentMapper.toDto(save.getAgent()));
        projectDatabaseDto.setMembers(databaseUserMapper.mapToDtoList(save.getMembers()));
        return projectDatabaseDto;
    }

    public void mapUpdate(ProjectDatabase projectDatabase, ProjectDatabaseUpdateDto dto) {
        projectDatabase.setName(dto.getName());
        projectDatabase.setDescription(dto.getDescription());
        projectDatabase.setAgent(projectAgentValidator.existsAndGet(dto.getAgentId()));
        projectDatabase.setMembers(repository.findAllByIdIn(dto.getMembersId()));
    }

    public List<ProjectDatabaseDto> toDtoList(String authUserId) {
        List<ProjectDatabase> allByAuthUserId = repository.findAllByAuthIdAndOrganizationId(authUserId);
        return allByAuthUserId
                .stream()
                .map(this::toDto)
                .toList();
    }
    public List<ProjectDatabaseDto> toDtoList(List<ProjectDatabase> all) {
        return all
                .stream()
                .map(this::toDto)
                .toList();
    }
}
