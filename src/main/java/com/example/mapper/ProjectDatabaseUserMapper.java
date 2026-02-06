package com.example.mapper;

import com.example.service.PasswordGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.special.AuthUserDbsResponse;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserUpdateDto;
import com.example.model.entity.AuthUser;
import com.example.model.entity.DatabaseRole;
import com.example.model.entity.ProjectDatabase;
import com.example.model.entity.ProjectDatabaseUser;
import com.example.repository.DatabaseRoleRepository;
import com.example.repository.ProjectDatabaseRepository;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.service.VersionProviderService;
import com.example.validator.AuthUserValidator;
import com.example.validator.ProjectDatabaseValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ProjectDatabaseUserMapper implements BaseMapper {
    private final ProjectDatabaseUserRepository repository;
    private final ProjectDatabaseRepository projectDatabaseRepository;
    private final VersionProviderService versionProviderService;
    private final DatabaseRoleRepository databaseRoleRepository;
    private final AuthUserValidator authUserValidator;
    private final AuthUserMapper authUserMapper;
    private final DatabaseRoleMapper databaseRoleMapper;
    private final ProjectDatabaseValidator projectDatabaseValidator;
    private final PasswordGenerator passwordGenerator;

    public List<ProjectDatabaseUserDto> mapToDtoList(List<ProjectDatabaseUser> all) {
        if (all == null) {
            return Collections.emptyList();
        }
        return all
                .stream()
                .filter(dbU -> !dbU.getDeleted())
                .map(this::toDto)
                .toList();
    }

    public ProjectDatabaseUserDto toDto(ProjectDatabaseUser databaseUser) {
        ProjectDatabaseUserDto databaseUserDto = new ProjectDatabaseUserDto();
        databaseUserDto.setId(databaseUser.getId());
        databaseUserDto.setDbPassword(databaseUser.getPassword());
        databaseUserDto.setDbUsername(databaseUser.getUsername());
        databaseUserDto.setDatabaseId(databaseUser.getDatabase().getId());
        if(databaseUser.getAuthUser()!=null) {
            databaseUserDto.setAuthUserDto(authUserMapper.toDto(databaseUser.getAuthUser()));
        }
        databaseUserDto.setRoles(databaseRoleMapper.toListDto(databaseUser.getRoles()));
        return databaseUserDto;
    }

    public ProjectDatabaseUser mapToEntityOnCreate(ProjectDatabaseUserCreateDto createDto) {
        ProjectDatabaseUser projectDatabaseUser = new ProjectDatabaseUser();
        ProjectDatabase database = projectDatabaseRepository.findById(createDto.getDatabaseId()).orElseThrow(() -> new RuntimeException("database not found"));
        AuthUser authUser = authUserValidator.existsAndGet(createDto.getAuthUserId());
        projectDatabaseValidator.checkIfMemberAlreadyExists(database,createDto.getAuthUserId());
        List<DatabaseRole> roles = databaseRoleRepository.findAllByIdIn(createDto.getRoles());
        projectDatabaseUser.setDatabase(database);
        projectDatabaseUser.setAuthUser(authUser);
        projectDatabaseUser.setPassword(passwordGenerator.generatePassword());
        projectDatabaseUser.setUsername(createDto.getDbUsername());
        projectDatabaseUser.setRoles(roles);
        projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(database));
        return projectDatabaseUser;
    }
    public ProjectDatabaseUser mapToEntityOnCreate(ProjectDatabaseUserCreateDto createDto, ProjectDatabase database,AuthUser authUser,List<DatabaseRole> roles, String password) {
        ProjectDatabaseUser projectDatabaseUser = new ProjectDatabaseUser();
        projectDatabaseUser.setDatabase(database);
        projectDatabaseUser.setAuthUser(authUser);
        projectDatabaseUser.setPassword(password);
        projectDatabaseUser.setUsername(createDto.getDbUsername());
        projectDatabaseUser.setRoles(roles);
        projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(database));
        return projectDatabaseUser;
    }

    public void mapUpdate(ProjectDatabaseUser projectDatabaseUser, ProjectDatabaseUserUpdateDto dto) {
        List<DatabaseRole> roles = databaseRoleRepository.findAllById(dto.getRoleIds());
        projectDatabaseUser.setPassword(dto.getDbPassword());
        projectDatabaseUser.setUsername(dto.getDbUsername());
        projectDatabaseUser.setRoles(roles);
        ProjectDatabase database = projectDatabaseRepository.findById(dto.getDatabaseId()).orElseThrow(() -> new RuntimeException("database not found"));
        projectDatabaseUser.setDatabase(database);
        projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(database));
    }

    public List<ProjectDatabaseUserDto> toListDto() {
        return repository.findAll()
                .stream()
                .filter(m->!m.getDeleted())
                .map(this::toDto)
                .toList();
    }

    public List<AuthUserDbsResponse> mapToAuthUserDbResponse(List<ProjectDatabase> allByMemberId,String authUserId) {
        return allByMemberId
                .stream()
                  .flatMap(db->db.getMembers().stream()
                    .filter(m->m.getAuthUser().getId().equals(authUserId)&& !m.getDeleted())
                      .map(m-> new AuthUserDbsResponse(
                              db.getName(),
                              m.getId(),
                              m.getUsername(),
                              m.getPassword(),
                              databaseRoleMapper.toListDto(m.getRoles()))))
                .      toList();
    }

    public void mapToDelete(ProjectDatabaseUser projectDatabaseUser) {
        projectDatabaseUser.setDeleted(true);
        projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(projectDatabaseUser.getDatabase()));
        repository.deleteMemberFromDatabase(projectDatabaseUser.getDatabase().getId(),projectDatabaseUser.getId());
    }

    public void mapPatchUpdatePassword(ProjectDatabaseUser projectDatabaseUser, ProjectDatabaseUserUpdateDto updateDto) {
        if (updateDto.getDbPassword() != null) {
            projectDatabaseUser.setPassword(updateDto.getDbPassword());
            projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(projectDatabaseUser.getDatabase()));
        }
    }

    public ProjectDatabaseUser mapToEntityOnCreateNoAuthId(ProjectDatabaseUserCreateDto createDto) {
        ProjectDatabaseUser projectDatabaseUser = new ProjectDatabaseUser();
        Optional<ProjectDatabase> byId = projectDatabaseRepository.findById(createDto.getDatabaseId());
        if (byId.isEmpty()) {
            throw new RuntimeException("database could not be found");
        }
        projectDatabaseUser.setDatabase(byId.get());
        projectDatabaseUser.setUsername(createDto.getDbUsername());
//        projectDatabaseUser.setRoles(databaseRoleRepository.getAllByName(createDto.getDatabaseId(),createDto.getRoles()));
        return projectDatabaseUser;
    }
}
