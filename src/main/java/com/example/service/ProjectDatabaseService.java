package com.example.service;

import org.springframework.stereotype.Service;
import com.example.mapper.ProjectDatabaseMapper;
import com.example.model.dto.database.ProjectDatabaseCreateDto;
import com.example.model.dto.database.ProjectDatabaseDto;
import com.example.model.dto.database.ProjectDatabaseUpdateDto;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectDatabaseRepository;
import com.example.validator.ProjectDatabaseValidator;

import java.util.List;
import java.util.Optional;

@Service

public class ProjectDatabaseService extends AbstractService<
        ProjectDatabaseRepository,
        ProjectDatabaseMapper,
        ProjectDatabaseValidator> implements CRUDService<ProjectDatabaseDto, ProjectDatabaseCreateDto, ProjectDatabaseUpdateDto,String>{

    public ProjectDatabaseService(ProjectDatabaseRepository repository, ProjectDatabaseMapper mapper, ProjectDatabaseValidator validator) {
        super(repository, mapper, validator);
    }

    @Override
    public ProjectDatabaseDto create(ProjectDatabaseCreateDto createDto) {
        validator.validateOnCreate(createDto);
        ProjectDatabase entity = mapper.toEntityFromCreate(createDto);
        ProjectDatabase save = repository.save(entity);
        return mapper.toDto(save);
    }

    @Override
    public ProjectDatabaseDto update(String id, ProjectDatabaseUpdateDto dto) {
        ProjectDatabase projectDatabase = validator.validateId(id);
        mapper.mapUpdate(projectDatabase,dto);
        return mapper.toDto(repository.save(projectDatabase));
    }

    @Override
    public ProjectDatabaseDto get(String id) {
        ProjectDatabase projectDatabase = validator.validateId(id);
        return mapper.toDto(projectDatabase);
    }

    @Override
    public List<ProjectDatabaseDto> getAll() {
        String authUserId = validator.validateAuthUserId();
        return mapper.toDtoList(authUserId);
    }

    @Override
    public void delete(String id) {
        ProjectDatabase projectDatabase = validator.validateId(id);
        projectDatabase.setDeleted(true);
        repository.save(projectDatabase);
    }

    public ProjectDatabase getByAgentId(String agentId) {
        Optional<ProjectDatabase> byAgentId = repository.findByAgentId(agentId);
        if (byAgentId.isEmpty()){
            throw new RuntimeException("Agent does not have any database");
        }
        return byAgentId.get();
    }


}
