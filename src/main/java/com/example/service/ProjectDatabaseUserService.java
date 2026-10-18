package com.example.service;

import com.example.model.dto.special.AuthUserDbsResponse;
import com.example.model.dto.special.DatabaseUserRegistry;
import com.example.exception.InvalidInput;
import com.example.exception.ObjectNotFound;
import com.example.util.DbPasswordPolicy;
import com.example.model.entity.*;
import com.example.repository.DatabaseRoleRepository;
import com.example.validator.AccessValidator;
import com.example.validator.AuthUserValidator;
import com.example.validator.ProjectDatabaseValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.example.events.SendEmailUpdateDBEvent;
import com.example.mapper.ProjectDatabaseUserMapper;
import com.example.model.dto.special.AuthUserDbsResponseDb;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserUpdateDto;
import com.example.repository.ProjectDatabaseRepository;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.validator.ProjectDatabaseUserValidator;

import java.util.List;
import java.util.Optional;

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
    private final ProjectDatabaseValidator projectDatabaseValidator;
    private final VersionProviderService versionProviderService;
    private final AccessValidator accessValidator;

    public ProjectDatabaseUserService(ProjectDatabaseUserRepository repository, ProjectDatabaseUserMapper mapper, ProjectDatabaseUserValidator validator, ProjectDatabaseRepository projectDatabaseRepository, ApplicationEventPublisher applicationEventPublisher, AuthUserValidator authUserValidator, DatabaseRoleRepository databaseRoleRepository, ProjectDatabaseValidator projectDatabaseValidator, VersionProviderService versionProviderService, AccessValidator accessValidator) {
        super(repository, mapper, validator);
        this.projectDatabaseRepository = projectDatabaseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.authUserValidator = authUserValidator;
        this.databaseRoleRepository = databaseRoleRepository;
        this.projectDatabaseValidator = projectDatabaseValidator;
        this.versionProviderService = versionProviderService;
        this.accessValidator = accessValidator;
    }


    @Override
    @Transactional
    public ProjectDatabaseUserDto create(ProjectDatabaseUserCreateDto createDto) {
        checkCreateAccess(createDto);
        validator.validateOnCreate(createDto);
        var databaseUser = reuseImportedLogin(createDto).orElseGet(() -> mapper.mapToEntityOnCreate(createDto));
        ProjectDatabaseUser savedMember = repository.save(databaseUser);
        ProjectDatabase database = databaseUser.getDatabase();
        if (!database.getMembers().contains(databaseUser)) {
            database.getMembers().add(databaseUser);
        }
        WebSettings settings = databaseUser.getAuthUser().getSettings();
        applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, savedMember.getAuthUser().getEmail(), "Hey there %s!\nNow you have access to database %s. Check out more data about it in your profile on db-controller.xyz".formatted(savedMember.getAuthUser().getUsername(), database.getName()), settings.getEnableEmailing()));
        projectDatabaseRepository.save(database);
        return mapper.toDto(databaseUser);
    }

    @Override
    public ProjectDatabaseUserDto update(String id, ProjectDatabaseUserUpdateDto dto) {
        ProjectDatabaseUser projectDatabaseUser = checkMember(validator.validateId(id));
        accessValidator.checkDatabase(projectDatabaseValidator.validateId(dto.getDatabaseId()));
        mapper.mapUpdate(projectDatabaseUser, dto);
        repository.save(projectDatabaseUser);
        try {

            WebSettings settings = projectDatabaseUser.getAuthUser().getSettings();
            applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, projectDatabaseUser.getAuthUser().getEmail(), "Hey there %s!\nThere were some changes to your roles on database %s. Check out more data about it in your profile on db-controller.xyz".formatted(projectDatabaseUser.getAuthUser().getUsername(), projectDatabaseUser.getDatabase().getName()), settings.getEnableEmailing()));
        } catch (RuntimeException e) {
            log.warn("UPDATE: user is not set yet so email sending is not executed!");
        }
        return mapper.toDto(projectDatabaseUser);
    }

    @Override
    public ProjectDatabaseUserDto get(String id) {
        ProjectDatabaseUser projectDatabaseUser = checkMember(validator.validateId(id));
        return mapper.toDto(projectDatabaseUser);
    }

    @Override
    public List<ProjectDatabaseUserDto> getAll() {
        return mapper.toListDto(accessValidator.currentOrganizationId());
    }

    @Override
    @Transactional
    public void delete(String id) {
        ProjectDatabaseUser projectDatabaseUser = checkMember(validator.validateId(id));
        mapper.mapToDelete(projectDatabaseUser);
        AuthUser authUser = projectDatabaseUser.getAuthUser();
        if (authUser!=null) {
            WebSettings settings = authUser.getSettings();
            try {
                applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, authUser.getEmail(), "Hey there %s!\nNow you have been restricted to access to database %s. Check out all databases you can access in your profile on db-controller.xyz".formatted(authUser.getUsername(), projectDatabaseUser.getDatabase().getName()), settings.getEnableEmailing()));
            } catch (RuntimeException e) {
                log.warn("DELETE: user is not set yet so email sending is not executed!");
            }
        }
        repository.save(projectDatabaseUser);
    }

    public AuthUserDbsResponse getAuthUserDatabases() {
        String authUserId = validator.validateAuthUserId();
        List<ProjectDatabase> allByMemberId = repository.findAllByMemberId(authUserId);
        List<AuthUserDbsResponseDb> authUserDbsResponseDbs = mapper.mapToAuthUserDbResponse(allByMemberId, authUserId);
        AuthUser authUser = authUserValidator.validateIdAndGet(authUserId);
        return new AuthUserDbsResponse(authUserDbsResponseDbs, authUser.getDbPassword());
    }

    public void updatePatch(String authUserId, String password) {
        // users may only change their own database password
        if (!authUserId.equals(accessValidator.currentUserId())) {
            throw new ObjectNotFound("user not found");
        }
        authUserValidator.validateId(authUserId);
        // the agent embeds this password in SQL on the customer's database, so allow only safe characters
        if (password != null && !password.isBlank() && !DbPasswordPolicy.isSafe(password)) {
            throw new InvalidInput("Password must be " + DbPasswordPolicy.ALLOWED);
        }
        mapper.mapPatchUpdatePassword(authUserId, password);
    }

    @Transactional
    public void registerUsers(ProjectDatabase database, List<DatabaseUserRegistry> dto) {
        for (DatabaseUserRegistry createDto : dto) {
            ProjectDatabaseUser noAuthIdCheck = createNoAuthIdCheck(createDto, database.getId());
            database.getMembers().add(noAuthIdCheck);
        }
        projectDatabaseRepository.save(database);
    }

    private ProjectDatabaseUser createNoAuthIdCheck(DatabaseUserRegistry createDto, String databaseId) {
        return repository.save(mapper.mapToEntityOnCreateNoAuthId(createDto, databaseId));
    }

    public void attachUser(String dbUserId, String authId) {
        ProjectDatabaseUser projectDatabaseUser = checkMember(validator.validateId(dbUserId));
        AuthUser authUser = accessValidator.checkUser(authUserValidator.validateIdAndGet(authId));
        projectDatabaseValidator.checkIfAuthUserAlreadyExists(projectDatabaseUser.getDatabase(), authUser);
        projectDatabaseUser.setAuthUser(authUser);
        projectDatabaseUser.setVersion(versionProviderService.getMaxVersionAndIncrement(projectDatabaseUser.getDatabase()));
        applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this,authUser.getEmail(),"Hi there you have been granted a database membership! For more more details log in to your account on DB-controller.com",authUser.getSettings().getEnableEmailing()));
        repository.save(projectDatabaseUser);
    }

    public void unattachUser(String dbUserId, String authId) {
        ProjectDatabaseUser projectDatabaseUser = checkMember(validator.validateId(dbUserId));
        AuthUser authUser = accessValidator.checkUser(authUserValidator.validateIdAndGet(authId));
        if (projectDatabaseUser.getAuthUser() == null || !projectDatabaseUser.getAuthUser().getId().equals(authUser.getId())) {
            throw new ObjectNotFound("member not found");
        }
        projectDatabaseUser.setAuthUser(null);
        applicationEventPublisher.publishEvent(new SendEmailUpdateDBEvent(this, authUser.getEmail(), "Hi there your membership for a database has been revoked! For more more details log in to your account on DB-controller.com", authUser.getSettings().getEnableEmailing()));
        repository.save(projectDatabaseUser);
    }

    public void removeUsers(ProjectDatabase database, List<String> removedUsers) {
        if (!removedUsers.isEmpty()) {
            repository.deleteMembersFromArray(database.getId(), removedUsers);
            repository.removeMembers(database.getId(), removedUsers);
        }

    }

    public void create(ProjectDatabaseUserCreateDto createDto, List<String> databaseId) {
        checkCreateAccess(createDto);
        validator.validateOnCreate(createDto);
        AuthUser authUser = authUserValidator.validateIdAndGet(createDto.getAuthUserId());
        List<ProjectDatabase> allById = projectDatabaseRepository.findAllById(databaseId);
        allById.forEach(accessValidator::checkDatabase);
        List<ProjectDatabase> databases = allById
                .stream()
                .filter(db -> db.getMembers().stream()
                        .map(ProjectDatabaseUser::getAuthUser)
                        .noneMatch(u -> u != null && !u.getId().equals(authUser.getId())))
                .toList();


        ProjectDatabaseUser user = repository.save(mapper.mapToEntityOnCreate(createDto));
        ProjectDatabase userDb = user.getDatabase();
        userDb.getMembers().add(user);
        projectDatabaseRepository.save(userDb);
        List<DatabaseRole> roles = databaseRoleRepository.findAllByIdIn(createDto.getRoles());
        for (ProjectDatabase database : databases) {
            createDto.setDatabaseId(database.getId());
            ProjectDatabaseUser projectDatabaseUser = mapper.mapToEntityOnCreate(createDto, database, authUser, roles, authUser.getPassword());
            database.getMembers().add(projectDatabaseUser);
            repository.save(projectDatabaseUser);
        }
        projectDatabaseRepository.saveAll(databases);
    }

    /**
     * The agent imports logins that already exist in the database as members without an account.
     * Adding that person attaches them to that row instead of creating a second member with the
     * same login (which made every later agent sync fail).
     */
    private Optional<ProjectDatabaseUser> reuseImportedLogin(ProjectDatabaseUserCreateDto createDto) {
        AuthUser authUser = authUserValidator.validateIdAndGet(createDto.getAuthUserId());
        if (authUser.getDbUsername() == null) {
            return Optional.empty();
        }
        List<ProjectDatabaseUser> sameLogin = repository.findAllByUsernameAndDatabaseId(authUser.getDbUsername(), createDto.getDatabaseId());
        Optional<ProjectDatabaseUser> imported = sameLogin.stream().filter(m -> m.getAuthUser() == null).findFirst();
        imported.ifPresent(member -> {
            projectDatabaseValidator.checkIfMemberAlreadyExists(member.getDatabase(), authUser.getId());
            member.setAuthUser(authUser);
            member.setRoles(databaseRoleRepository.findAllByIdIn(createDto.getRoles()).stream()
                    .filter(r -> createDto.getDatabaseId().equals(r.getDatabaseId()))
                    .toList());
            member.setVersion(versionProviderService.getMaxVersionAndIncrement(member.getDatabase()));
        });
        return imported;
    }

    private ProjectDatabaseUser checkMember(ProjectDatabaseUser member) {
        accessValidator.checkDatabase(member.getDatabase());
        return member;
    }

    private void checkCreateAccess(ProjectDatabaseUserCreateDto createDto) {
        accessValidator.checkDatabase(projectDatabaseValidator.validateId(createDto.getDatabaseId()));
        accessValidator.checkUser(authUserValidator.validateIdAndGet(createDto.getAuthUserId()));
    }
}
