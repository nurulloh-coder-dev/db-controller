package com.example.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.mapper.ProjectAgentMapper;
import com.example.model.dto.agent.ProjectAgentCreateDTO;
import com.example.model.dto.agent.ProjectAgentDTO;
import com.example.model.dto.agent.ProjectAgentUpdateDTO;
import com.example.model.dto.database.ProjectDatabaseCreateDto;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.entity.Organization;
import com.example.model.entity.ProjectAgent;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectAgentRepository;
import com.example.validator.ProjectAgentValidator;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectAgentService extends AbstractService<ProjectAgentRepository, ProjectAgentMapper, ProjectAgentValidator>
        implements CRUDService<ProjectAgentDTO, ProjectAgentCreateDTO, ProjectAgentUpdateDTO, String> {

    private final ProjectDatabaseService projectDatabaseService;
    private final ProjectDatabaseUserService projectDatabaseUserService;
    private final DatabaseRoleService databaseRoleService;

    public ProjectAgentService(ProjectAgentRepository repository, ProjectAgentMapper mapper, ProjectAgentValidator validator, ProjectDatabaseService projectDatabaseService, ProjectDatabaseUserService projectDatabaseUserService, DatabaseRoleService databaseRoleService) {
        super(repository, mapper, validator);
        this.projectDatabaseService = projectDatabaseService;
        this.projectDatabaseUserService = projectDatabaseUserService;
        this.databaseRoleService = databaseRoleService;
    }

    @Override
    public ProjectAgentDTO create(ProjectAgentCreateDTO dto) {
        validator.validateOnCreate(dto);
        ProjectAgent entityOnCreate = mapper.toEntityOnCreate(dto);
        return mapper.toDto(repository.save(entityOnCreate));
    }

    @Override
    public ProjectAgentDTO update(String id, ProjectAgentUpdateDTO dto) {
        ProjectAgent projectAgent = validator.existsAndGet(id);
        projectAgent.setName(dto.getName());
        projectAgent.setDatabaseUsername(dto.getDatabaseUsername());
        projectAgent.setDatabaseUrl(dto.getDatabaseUrl());
        return mapper.toDto(repository.save(projectAgent));
    }

    @Override
    public ProjectAgentDTO get(String id) {
        return repository.findById(id).map(mapper::toDto).orElseThrow(() -> new NoSuchElementException("Project agent with id " + id + " not found."));
    }

    @Override
    public List<ProjectAgentDTO> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public void delete(String id) {
        ProjectAgent projectAgent = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Project agent with id " + id + " not found."));
        projectAgent.setDeleted(true);
        repository.save(projectAgent);
    }

    public ProjectAgentDTO getAgentByDBUrl(String dbUrl) {
        String decodedUrl = URLDecoder.decode(dbUrl);
        Optional<ProjectAgent> byDatabaseUrl = repository.findByDatabaseUrl(decodedUrl);
        if (byDatabaseUrl.isPresent()) {
            return mapper.toDto(byDatabaseUrl.get());
        } else {
            throw new NoSuchElementException("Project agent with database url " + dbUrl + " not found.");
        }
    }

    @Transactional
    public ProjectAgentDTO createWithDb(ProjectAgentCreateDTO dto) {
        Optional<ProjectAgent> byId = repository.findByDatabaseNameAndCompanyId(dto.getName(), dto.getOrganizationId());
        if (byId.isPresent()) {
            ProjectAgent projectAgent = byId.get();
            return new ProjectAgentDTO(projectAgent.getId(), projectAgent.getName(), projectAgent.getDatabaseUsername(), projectAgent.getDatabasePassword(), projectAgent.getDatabaseUrl(), dto.getVersion());
        }
        Organization organization = validator.validateOrganizationId(dto.getOrganizationId());
        ProjectAgentDTO projectAgentDTO = create(dto);
        projectDatabaseService.create(ProjectDatabaseCreateDto.builder()
                .agentId(projectAgentDTO.getId())
                .name(projectAgentDTO.getName())
                .organizationId(organization.getId())
                .membersId(Collections.emptyList())
                .version(dto.getVersion())
                .build());
        return projectAgentDTO;
    }

    public void updateRoles(String agentId, List<DatabaseRoleCreateDTO> roles) {
        Boolean agentExists = validator.validateId(agentId);
        if (!agentExists) {
            throw new RuntimeException("Agent not found");
        }
        ProjectDatabase database = projectDatabaseService.getByAgentId(agentId);
        validator.validateIfAnyRoleActive(database, roles);
        mapper.mapUpdate(database, roles);
    }

    public void registerUsers(String agentId, List<ProjectDatabaseUserCreateDto> dto) {
        ProjectDatabase database= validator.validateIdAndGetDatabaseId(agentId);
        List<String> usernamesByDatabaseId = repository.findUsernamesByDatabaseId(database.getId());
        List<ProjectDatabaseUserCreateDto> newUsers = dto
                .stream()
                .filter(r -> !usernamesByDatabaseId.contains(r.getDbUsername()))
                .toList();

        List<String> list = dto.stream().map(ProjectDatabaseUserCreateDto::getDbUsername).toList();

        List<String> removedUsers = usernamesByDatabaseId
                .stream()
                .filter(u -> !list.contains(u))
                .toList();


        projectDatabaseUserService.registerUsers(database, newUsers);
        projectDatabaseUserService.removeUsers(database,removedUsers);
        databaseRoleService.syncUserRoles(dto);
    }
}
