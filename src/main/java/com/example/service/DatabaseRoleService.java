package com.example.service;

import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.special.DatabaseUserRegistry;
import com.example.model.entity.ProjectDatabaseUser;
import com.example.repository.ProjectDatabaseUserRepository;
import org.springframework.stereotype.Service;
import com.example.validator.AccessValidator;
import com.example.validator.ProjectDatabaseValidator;
import com.example.mapper.DatabaseRoleMapper;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import com.example.model.entity.DatabaseRole;
import com.example.repository.DatabaseRoleRepository;
import com.example.validator.DatabaseRoleValidator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DatabaseRoleService extends AbstractService<DatabaseRoleRepository, DatabaseRoleMapper, DatabaseRoleValidator>
        implements CRUDService<DatabaseRoleDTO, DatabaseRoleCreateDTO, DatabaseRoleDTO, String> {
    private final ProjectDatabaseUserRepository projectDatabaseUserRepository;
    private final ProjectDatabaseValidator projectDatabaseValidator;
    private final AccessValidator accessValidator;

    public DatabaseRoleService(DatabaseRoleRepository repository, DatabaseRoleMapper mapper, DatabaseRoleValidator validator, ProjectDatabaseUserRepository projectDatabaseUserRepository, ProjectDatabaseValidator projectDatabaseValidator, AccessValidator accessValidator) {
        super(repository, mapper, validator);
        this.projectDatabaseUserRepository = projectDatabaseUserRepository;
        this.projectDatabaseValidator = projectDatabaseValidator;
        this.accessValidator = accessValidator;
    }

    @Override
    public DatabaseRoleDTO create(DatabaseRoleCreateDTO dto) {
        return mapper.toDto(repository.save(mapper.toEntityOnCreate(dto)));
    }

    @Override
    public DatabaseRoleDTO update(String id, DatabaseRoleDTO dto) {
        DatabaseRole databaseRole = checkRole(repository.findById(id).orElseThrow(() -> new NoSuchElementException("Database role with id " + id + " not found")));
        databaseRole.setName(dto.getName());
        databaseRole.setDescription(dto.getDescription());
        databaseRole.setCode(dto.getCode());
        return mapper.toDto(repository.save(databaseRole));
    }

    @Override
    public DatabaseRoleDTO get(String id) {
        return repository.findById(id).map(this::checkRole).map(mapper::toDto).orElseThrow(() -> new NoSuchElementException("Database role with id " + id + " not found."));
    }

    @Override
    public List<DatabaseRoleDTO> getAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public void delete(String id) {
        DatabaseRole databaseRole = checkRole(repository.findById(id).orElseThrow(() -> new NoSuchElementException("Database role with id " + id + " not found.")));
        databaseRole.setDeleted(true);
        repository.save(databaseRole);
    }

    public List<DatabaseRoleDTO> getAllByDBId(String id) {
        accessValidator.checkDatabase(projectDatabaseValidator.validateId(id));
        return mapper.toListDto(repository.findAllByDatabaseId(id));
    }

    public void syncUserRoles(List<DatabaseUserRegistry> dto,String databaseId) {
        for (DatabaseUserRegistry user : dto) {
            // normally one row per login; older data can hold duplicates, so update them all
            for (ProjectDatabaseUser projectDatabaseUser : projectDatabaseUserRepository.findAllByUsernameAndDatabaseId(user.getUsername(), databaseId)) {
                projectDatabaseUser.setRoles(repository.getAllByName(databaseId, user.getRoles()));
                projectDatabaseUserRepository.save(projectDatabaseUser);
            }
        }
    }

    private DatabaseRole checkRole(DatabaseRole role) {
        if (role.getDatabaseId() == null) {
            throw new NoSuchElementException("Database role with id " + role.getId() + " not found.");
        }
        accessValidator.checkDatabase(projectDatabaseValidator.validateId(role.getDatabaseId()));
        return role;
    }
}
