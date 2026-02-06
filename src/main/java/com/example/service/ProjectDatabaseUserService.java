package com.example.service;

import com.example.model.entity.*;
import com.example.repository.DatabaseRoleRepository;
import com.example.validator.AuthUserValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.example.events.SendEmailUpdateDBEvent;
import com.example.mapper.ProjectDatabaseUserMapper;
import com.example.model.dto.special.AuthUserDbsResponse;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserUpdateDto;
import com.example.repository.ProjectDatabaseRepository;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.validator.ProjectDatabaseUserValidator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ProjectDatabaseUserService extends AbstractService<
        ProjectDatabaseUserRepository,
        ProjectDatabaseUserMapper,
        ProjectDatabaseUserValidator> implements CRUDService<ProjectDatabaseUserDto, ProjectDatabaseUserCreateDto, ProjectDatabaseUserUpdateDto, String> {

    private final ProjectDatabaseRepository projectDatabaseRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthUserValidator authUserValidator;
    private final DatabaseRoleRepository databaseRoleRepository;

    public ProjectDatabaseUserService(ProjectDatabaseUserRepository repository, ProjectDatabaseUserMapper mapper, ProjectDatabaseUserValidator validator, ProjectDatabaseRepository projectDatabaseRepository, ApplicationEventPublisher applicationEventPublisher, AuthUserValidator authUserValidator, DatabaseRoleRepository databaseRoleRepository) {
        super(repository, mapper, validator);
        this.projectDatabaseRepository = projectDatabaseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.authUserValidator = authUserValidator;
        this.databaseRoleRepository = databaseRoleRepository;
    }


    @Override
    @Transactional
    public ProjectDatabaseUserDto create(ProjectDatabaseUserCreateDto createDto) {
        validator.validateOnCreate(createDto);
        var databaseUser = mapper.mapToEntityOnCreate(createDto);
        ProjectDatabaseUser savedMember = repository.save(databaseUser);
        ProjectDatabase database = databaseUser.getDatabase();
        database.getMembers().add(databaseUser);
        WebSettings settings = databaseUser.getAuthUser().getSettings();
        applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, savedMember.getAuthUser().getEmail(), "Hey there %s!\nNow you have access to database %s. Check out more data about it in your profile on Hub.com".formatted(savedMember.getAuthUser().getUsername(), database.getName()),settings.getEnableEmailing()));
        projectDatabaseRepository.save(database);
        return mapper.toDto(databaseUser);
    }

    @Override
    public ProjectDatabaseUserDto update(String id, ProjectDatabaseUserUpdateDto dto) {
        ProjectDatabaseUser projectDatabaseUser = validator.validateId(id);
        mapper.mapUpdate(projectDatabaseUser, dto);
        repository.save(projectDatabaseUser);
        try {

            WebSettings settings = projectDatabaseUser.getAuthUser().getSettings();
            applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, projectDatabaseUser.getAuthUser().getEmail(), "Hey there %s!\nThere were some changes to your roles on database %s. Check out more data about it in your profile on Hub.com".formatted(projectDatabaseUser.getAuthUser().getUsername(), projectDatabaseUser.getDatabase().getName()),settings.getEnableEmailing()));
        }catch (RuntimeException e){
            log.warn("UPDATE: user is not set yet so email sending is not executed!");
        }
        return mapper.toDto(projectDatabaseUser);
    }

    @Override
    public ProjectDatabaseUserDto get(String id) {
        ProjectDatabaseUser projectDatabaseUser = validator.validateId(id);
        return mapper.toDto(projectDatabaseUser);
    }

    @Override
    public List<ProjectDatabaseUserDto> getAll() {
        return mapper.toListDto();
    }

    @Override
    @Transactional
    public void delete(String id) {
        ProjectDatabaseUser projectDatabaseUser = validator.validateId(id);
        mapper.mapToDelete(projectDatabaseUser);
        WebSettings settings = projectDatabaseUser.getAuthUser().getSettings();
        try {
            applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, projectDatabaseUser.getAuthUser().getEmail(), "Hey there %s!\nNow you have been restricted to access to database %s. Check out all databases you can access in your profile on Hub.com".formatted(projectDatabaseUser.getAuthUser().getUsername(), projectDatabaseUser.getDatabase().getName()),settings.getEnableEmailing()));
        }catch (RuntimeException e){
            log.warn("DELETE: user is not set yet so email sending is not executed!");
        }
        repository.save(projectDatabaseUser);
    }

    public List<AuthUserDbsResponse> getAuthUserDatabases() {
        String authUserId = validator.validateAuthUserId();
        List<ProjectDatabase> allByMemberId = repository.findAllByMemberId(authUserId);
        return mapper.mapToAuthUserDbResponse(allByMemberId,authUserId);
    }

    public ProjectDatabaseUserDto updatePatch(String id, ProjectDatabaseUserUpdateDto updateDto) {
        ProjectDatabaseUser projectDatabaseUser = validator.validateId(id);
        mapper.mapPatchUpdatePassword(projectDatabaseUser, updateDto);
        return mapper.toDto(repository.save(projectDatabaseUser));
    }

    @Transactional
    public void registerUsers(ProjectDatabase database, List<ProjectDatabaseUserCreateDto> dto) {
        for (ProjectDatabaseUserCreateDto createDto : dto) {
            createDto.setDatabaseId(database.getId());
            ProjectDatabaseUser noAuthIdCheck = createNoAuthIdCheck(createDto);
            database.getMembers().add(noAuthIdCheck);
        }
        projectDatabaseRepository.save(database);
    }

    private ProjectDatabaseUser createNoAuthIdCheck(ProjectDatabaseUserCreateDto createDto) {
        return repository.save(mapper.mapToEntityOnCreateNoAuthId(createDto));
    }

    public void attachUser(String dbUserId, String authId) {
        ProjectDatabaseUser projectDatabaseUser = validator.validateId(dbUserId);
        AuthUser authUser = authUserValidator.existsAndGet(authId);
        projectDatabaseUser.setAuthUser(authUser);
        repository.save(projectDatabaseUser);
    }

    public void removeUsers(ProjectDatabase database, List<String> removedUsers) {
        if (!removedUsers.isEmpty()) {
            repository.deleteMembersFromArray(database.getId(), removedUsers);
            repository.removeMembers(database.getId(),removedUsers);
        }

    }

    public void create(ProjectDatabaseUserCreateDto createDto, List<String> databaseId) {
        validator.validateOnCreate(createDto);
        String dbUsername = createDto.getDbUsername();
        List<ProjectDatabase> allById = projectDatabaseRepository.findAllById(databaseId);
        List<ProjectDatabase> databases = allById
                .stream()
                .filter(db -> db.getMembers().stream()
                        .map(ProjectDatabaseUser::getUsername)
                        .noneMatch(u -> u != null && u.contains(dbUsername)))
                .toList();

        ProjectDatabaseUser user = repository.save(mapper.mapToEntityOnCreate(createDto));
        ProjectDatabase userDb = user.getDatabase();
        userDb.getMembers().add(user);
        projectDatabaseRepository.save(userDb);
        String password = user.getPassword();
        List<DatabaseRole> roles = databaseRoleRepository.findAllByIdIn(createDto.getRoles());
        AuthUser authUser = authUserValidator.existsAndGet(createDto.getAuthUserId());
        for (ProjectDatabase database : databases) {
            createDto.setDatabaseId(database.getId());
            ProjectDatabaseUser projectDatabaseUser = mapper.mapToEntityOnCreate(createDto, database,authUser,roles,password);
            database.getMembers().add(projectDatabaseUser);
            repository.save(projectDatabaseUser);
        }
        projectDatabaseRepository.saveAll(databases);
    }
}
