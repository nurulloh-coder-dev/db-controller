package com.example.service;

import com.example.model.dto.special.DatabaseUserRegistry;
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
import com.example.validator.AccessValidator;
import com.example.validator.ProjectAgentValidator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProjectAgentService extends AbstractService<ProjectAgentRepository, ProjectAgentMapper, ProjectAgentValidator>
        implements CRUDService<ProjectAgentDTO, ProjectAgentCreateDTO, ProjectAgentUpdateDTO, String> {

    private final ProjectDatabaseService projectDatabaseService;
    private final ProjectDatabaseUserService projectDatabaseUserService;
    private final DatabaseRoleService databaseRoleService;
    private final AccessValidator accessValidator;
    private final PasswordEncoder passwordEncoder;

    public ProjectAgentService(ProjectAgentRepository repository, ProjectAgentMapper mapper, ProjectAgentValidator validator, ProjectDatabaseService projectDatabaseService, ProjectDatabaseUserService projectDatabaseUserService, DatabaseRoleService databaseRoleService, AccessValidator accessValidator, PasswordEncoder passwordEncoder) {
        super(repository, mapper, validator);
        this.projectDatabaseService = projectDatabaseService;
        this.projectDatabaseUserService = projectDatabaseUserService;
        this.databaseRoleService = databaseRoleService;
        this.accessValidator = accessValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProjectAgentDTO create(ProjectAgentCreateDTO dto) {
        dto.setOrganizationId(accessValidator.currentOrganizationId());
        return save(dto);
    }

    private ProjectAgentDTO save(ProjectAgentCreateDTO dto) {
        validator.validateOnCreate(dto);
        ProjectAgent entityOnCreate = mapper.toEntityOnCreate(dto);
        entityOnCreate.setDatabasePassword(hashOrNull(dto.getDatabasePassword()));
        return mapper.toDto(repository.save(entityOnCreate));
    }

    @Override
    public ProjectAgentDTO update(String id, ProjectAgentUpdateDTO dto) {
        ProjectAgent projectAgent = accessValidator.checkAgent(validator.existsAndGet(id));
        projectAgent.setName(dto.getName());
        projectAgent.setDatabaseUsername(dto.getDatabaseUsername());
        projectAgent.setDatabaseUrl(dto.getDatabaseUrl());
        return mapper.toDto(repository.save(projectAgent));
    }

    @Override
    public ProjectAgentDTO get(String id) {
        return repository.findById(id).map(accessValidator::checkAgent).map(mapper::toDto).orElseThrow(() -> new NoSuchElementException("Project agent with id " + id + " not found."));
    }

    @Override
    public List<ProjectAgentDTO> getAll() {
        String organizationId = accessValidator.currentOrganizationId();
        return repository.findAll().stream()
                .filter(a -> organizationId.equals(a.getOrganizationId()))
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public void delete(String id) {
        ProjectAgent projectAgent = repository.findById(id).map(accessValidator::checkAgent).orElseThrow(() -> new NoSuchElementException("Project agent with id " + id + " not found."));
        projectAgent.setDeleted(true);
        repository.save(projectAgent);
    }

    public ProjectAgentDTO getAgentByDBUrl(String dbUrl) {
        String decodedUrl = URLDecoder.decode(dbUrl);
        Optional<ProjectAgent> byDatabaseUrl = repository.findByDatabaseUrl(decodedUrl).map(accessValidator::checkAgent);
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
            // only an agent that knows the database password may take over an existing registration
            verifyAgentPassword(projectAgent, dto.getDatabasePassword());
            return new ProjectAgentDTO(projectAgent.getId(), projectAgent.getName(), projectAgent.getDatabaseUsername(), projectAgent.getDatabaseUrl(), dto.getVersion());
        }
        Organization organization = validator.validateOrganizationId(dto.getOrganizationId());
        ProjectAgentDTO projectAgentDTO = save(dto);
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

    public void registerUsers(String agentId, List<DatabaseUserRegistry> dto) {
        ProjectDatabase database = validator.validateIdAndGetDatabaseId(agentId);
        List<String> usernamesByDatabaseId = repository.findUsernamesByDatabaseId(database.getId());
        List<DatabaseUserRegistry> newUsers = dto
                .stream()
                .filter(r -> !usernamesByDatabaseId.contains(r.getUsername()))
                .toList();

        List<String> list = dto.stream().map(DatabaseUserRegistry::getUsername).toList();

        List<String> removedUsers = usernamesByDatabaseId
                .stream()
                .filter(u -> !list.contains(u))
                .toList();


        projectDatabaseUserService.registerUsers(database, newUsers);
        projectDatabaseUserService.removeUsers(database, removedUsers);
        databaseRoleService.syncUserRoles(dto, database.getId());
    }

    private void verifyAgentPassword(ProjectAgent agent, String password) {
        String stored = agent.getDatabasePassword();
        if (stored == null) {
            // registered before passwords were recorded: remember this one for next time
            if (password != null) {
                agent.setDatabasePassword(passwordEncoder.encode(password));
                repository.save(agent);
            }
            return;
        }
        if (password == null) {
            throw new AccessDeniedException("Agent credentials do not match");
        }
        if (stored.startsWith("$2")) {
            if (!passwordEncoder.matches(password, stored)) {
                throw new AccessDeniedException("Agent credentials do not match");
            }
            return;
        }
        // legacy plaintext value: compare, then upgrade to a hash
        if (!stored.equals(password)) {
            throw new AccessDeniedException("Agent credentials do not match");
        }
        agent.setDatabasePassword(passwordEncoder.encode(password));
        repository.save(agent);
    }

    private String hashOrNull(String password) {
        return password == null ? null : passwordEncoder.encode(password);
    }
}
