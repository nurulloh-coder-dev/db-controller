package com.example.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.events.SendEmailToAuthUser;
import com.example.events.SendEmailUpdateDBEvent;
import com.example.model.dto.special.AuthUserCreateWithOrganization;
import com.example.model.entity.AuthUser;
import com.example.mapper.AuthUserMapper;
import com.example.model.dto.authUser.AuthUserDto;
import com.example.model.dto.authUser.AuthUserCreateDto;
import com.example.model.dto.authUser.AuthUserUpdateDto;
import com.example.model.entity.ProjectDatabaseUser;
import com.example.model.entity.WebSettings;
import com.example.repository.AuthUserRepository;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.validator.AccessValidator;
import com.example.validator.AuthUserValidator;

import com.example.exception.InvalidInput;
import java.time.Duration;
import java.util.List;

@Service
public class UserService
        extends AbstractService<
        AuthUserRepository,
        AuthUserMapper,
        AuthUserValidator>
        implements CRUDService<AuthUserDto, AuthUserCreateDto, AuthUserUpdateDto, String> {

    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserValidator authUserValidator;
    private final ProjectDatabaseUserService projectDatabaseUserService;
    private final ProjectDatabaseUserRepository projectDatabaseUserRepository;
    private final AccessValidator accessValidator;
    private final PasswordTokenService passwordTokenService;
    private final AuthService authService;

    public UserService(AuthUserRepository repository, AuthUserMapper mapper, AuthUserValidator validator, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, AuthUserValidator authUserValidator, ProjectDatabaseUserService projectDatabaseUserService, ProjectDatabaseUserRepository projectDatabaseUserRepository, AccessValidator accessValidator, PasswordTokenService passwordTokenService, AuthService authService) {
        super(repository, mapper, validator);
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
        this.authUserValidator = authUserValidator;
        this.projectDatabaseUserService = projectDatabaseUserService;
        this.projectDatabaseUserRepository = projectDatabaseUserRepository;
        this.accessValidator = accessValidator;
        this.passwordTokenService = passwordTokenService;
        this.authService = authService;
    }

    @Override
    @Transactional
    public AuthUserDto create(AuthUserCreateDto dto) {
        validator.validateOnCreate(dto);
        AuthUser authUser = mapper.fromDto(dto);
        // the generated password is never shown to anyone; the user picks their own through the link
        authUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        sendWelcomeEmail(authUser);
        return mapper.toDto(repository.save(authUser));
    }

    @Override
    public AuthUserDto update(String id, AuthUserUpdateDto dto) {
        AuthUser authUser = accessValidator.checkUser(validator.validateIdAndGet(id));
        validator.validateOnUpdate(dto,id);
        mapper.fromDto(dto, authUser);
        publisher.publishEvent(new SendEmailUpdateDBEvent(this, authUser.getEmail(), "Hey there %s!\n Your role has been recently changed to %s. For more info login to your account!".formatted(authUser.getUsername(), authUser.getRole()), authUser.getSettings().getEnableEmailing()));
        return mapper.toDto(repository.save(authUser));
    }

    @Override
    public AuthUserDto get(String id) {
        AuthUser authUser = accessValidator.checkUser(validator.validateIdAndGet(id));
        return mapper.toDto(authUser);
    }

    @Override
    public List<AuthUserDto> getAll() {
        List<AuthUser> all = mapper.findAllByCompany();
        return mapper.toDto(all);
    }

    public List<AuthUserDto> getAllBySearch(String search) {
        List<AuthUser> all = mapper.findAllByNameLike(search);
        return mapper.toDto(all);
    }
    public List<AuthUserDto> getAllBySearch(String search,String dbId) {
        List<AuthUser> all = mapper.findAllByNameLike(search,dbId);
        return mapper.toDto(all);
    }

    @Override
    public void delete(String id) {
        AuthUser authUser = accessValidator.checkUser(validator.validateIdAndGet(id));
        validator.deleteDatabaseMembership(id);
        projectDatabaseUserRepository.deleteMemberFromDatabaseByAuthUser(id);
        List<ProjectDatabaseUser> allByAuthUser =
                projectDatabaseUserRepository.getAllByAuthUser(id);

        for (ProjectDatabaseUser projectDatabaseUser : allByAuthUser) {
            projectDatabaseUserService.delete(projectDatabaseUser.getId());
        }
        authUser.setDeleted(true);
        repository.save(authUser);
    }

    public AuthUserDto updateIgnoreNull(String id, AuthUserUpdateDto dto) {
        AuthUser authUser = accessValidator.checkUser(validator.validateIdAndGet(id));
        // users may edit their own profile; editing others or changing a role needs an admin
        if (!authUser.getId().equals(accessValidator.currentUserId()) || dto.getRoleId() != null) {
            accessValidator.requireAdmin();
        }
        mapper.fromDtoIgnoreNull(dto, authUser);
        return mapper.toDto(repository.save(authUser));
    }

    public AuthUserDto getProfile() {
        String authUserId = authUserValidator.validateAuthenticationAndGetId();
        AuthUserDto profile = get(authUserId);
        profile.setSuperAdmin(accessValidator.isSuperAdmin());
        return profile;
    }

    public void changeSettings(WebSettings settings) {
        AuthUser authUser = validator.validateIdAndGet(validator.validateAuthenticationAndGetId());
        mapper.mapSettingsPatch(authUser, settings);
        repository.save(authUser);
    }

    public Boolean handlePasswordCheck(String password) {
        AuthUser authUser = validator.validateIdAndGet(validator.validateAuthenticationAndGetId());
        return password != null && passwordEncoder.matches(password, authUser.getPassword());
    }

    /** Changing the login password needs the current one, so a stolen session token isn't enough. */
    public void handlePasswordUpdate(String currentPassword, String newPassword) {
        AuthUser authUser = validator.validateIdAndGet(validator.validateAuthenticationAndGetId());
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, authUser.getPassword())) {
            throw new InvalidInput("Current password is incorrect");
        }
        authService.setPassword(authUser, newPassword);
    }

    private void sendWelcomeEmail(AuthUser user) {
        String link = passwordTokenService.createLink(user.getEmail(), Duration.ofDays(3));
        String text = """
                Hey there!
                You have been registered to DB-Controller with the username: %s

                Set your password here (the link is valid for 3 days and works once):
                %s

                If the link has expired, use "Forgot password" on the login page.""".formatted(user.getUsername(), link);
        publisher.publishEvent(new SendEmailToAuthUser(this, user.getEmail(), text, "Welcome to DB-Controller"));
    }

    @Transactional
    public AuthUserDto createWithOrganization(AuthUserCreateWithOrganization dto, String lang) {
        validator.validateOnCreateWithOrganization(dto,lang);
        AuthUser user = mapper.toEntityFromCreateAuthUserWithOrganization(dto,lang);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sendWelcomeEmail(user);
        return mapper.toDto(repository.save(user));
    }
}
