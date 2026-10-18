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
import lombok.SneakyThrows;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
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
    private static final SecureRandom RANDOM = new SecureRandom();

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
            ProjectAgentDTO existing = new ProjectAgentDTO(projectAgent.getId(), projectAgent.getName(), projectAgent.getDatabaseUsername(), projectAgent.getDatabaseUrl(), dto.getVersion(), null);
            issueAgentKey(projectAgent, existing, dto);
            return existing;
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
        issueAgentKey(validator.existsAndGet(projectAgentDTO.getId()), projectAgentDTO, dto);
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

    /**
     * Gives an agent that can store it a fresh random key (rotated on every registration).
     * Only its hash is kept; agents that never received a key keep working without one.
     */
    private void issueAgentKey(ProjectAgent agent, ProjectAgentDTO response, ProjectAgentCreateDTO request) {
        if (!request.isSupportsAgentKey()) {
            return;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        agent.setAgentKeyHash(sha256(key));
        repository.save(agent);
        response.setAgentKey(key);
    }

    /**
     * Checks the X-Agent-Key header for agent calls. Unknown or deleted agents are rejected;
     * agents that were issued a key must present it.
     */
    public void verifyAgentKey(String agentId, String agentKey) {
        ProjectAgent agent = repository.findByIdAndDeleted(agentId, false)
                .orElseThrow(() -> new AccessDeniedException("Unknown agent"));
        String expected = agent.getAgentKeyHash();
        if (expected == null) {
            return; // agent built before keys existed
        }
        if (agentKey == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), sha256(agentKey).getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Invalid agent key");
        }
    }

    @SneakyThrows
    private static String sha256(String value) {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private String hashOrNull(String password) {
        return password == null ? null : passwordEncoder.encode(password);
    }
}
