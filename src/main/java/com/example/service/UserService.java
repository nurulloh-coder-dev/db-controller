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
import com.example.validator.AuthUserValidator;

import java.util.Base64;
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

    public UserService(AuthUserRepository repository, AuthUserMapper mapper, AuthUserValidator validator, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, AuthUserValidator authUserValidator, ProjectDatabaseUserService projectDatabaseUserService, ProjectDatabaseUserRepository projectDatabaseUserRepository) {
        super(repository, mapper, validator);
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
        this.authUserValidator = authUserValidator;
        this.projectDatabaseUserService = projectDatabaseUserService;
        this.projectDatabaseUserRepository = projectDatabaseUserRepository;
    }

    @Override
    @Transactional
    public AuthUserDto create(AuthUserCreateDto dto) {
        validator.validateOnCreate(dto);
        AuthUser authUser = mapper.fromDto(dto);
        publisher.publishEvent(new SendEmailToAuthUser(this, authUser.getEmail(), "Hey there!\nYou have been successfully registered to Hub!🤗🤗🤗\nHere are details for login: username =  %s and password = %s for login. DO NOT SHARE IT WITH ANYONE!!!".formatted(authUser.getUsername(), authUser.getPassword()), "Welcome to Hub.com"));
        authUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        return mapper.toDto(repository.save(authUser));
    }

    @Override
    public AuthUserDto update(String id, AuthUserUpdateDto dto) {
        AuthUser authUser = validator.existsAndGet(id);
        validator.validateOnUpdate(dto,id);
        mapper.fromDto(dto, authUser);
        publisher.publishEvent(new SendEmailUpdateDBEvent(this, authUser.getEmail(), "Hey there %s!\n Your role has been recently changed to %s. For more info login to your account!".formatted(authUser.getUsername(), authUser.getRole()), authUser.getSettings().getEnableEmailing()));
        return mapper.toDto(repository.save(authUser));
    }

    @Override
    public AuthUserDto get(String id) {
        AuthUser authUser = validator.existsAndGet(id);
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

    @Override
    public void delete(String id) {
        AuthUser authUser = validator.existsAndGet(id);
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
        AuthUser authUser = validator.existsAndGet(id);
        mapper.fromDtoIgnoreNull(dto, authUser);
        return mapper.toDto(repository.save(authUser));
    }

    public AuthUserDto getProfile() {
        String authUserId = authUserValidator.validateAuthenticationAndGetId();
        return get(authUserId);
    }

    public void changeSettings(WebSettings settings) {
        AuthUser authUser = validator.existsAndGet(validator.validateAuthenticationAndGetId());
        mapper.mapSettingsPatch(authUser, settings);
        repository.save(authUser);
    }

    public Boolean handlePasswordCheck(String encodedPassword) {
        AuthUser authUser = validator.existsAndGet(validator.validateAuthenticationAndGetId());
        String password = new String(Base64.getDecoder().decode(encodedPassword));
        return passwordEncoder.matches(password, authUser.getPassword());
    }

    public void handlePasswordUpdate(String encodedPassword) {
        AuthUser authUser = validator.existsAndGet(validator.validateAuthenticationAndGetId());
        String password = new String(Base64.getDecoder().decode(encodedPassword));
        authUser.setPassword(passwordEncoder.encode(password));
        repository.save(authUser);
    }

    @Transactional
    public AuthUserDto createWithOrganization(AuthUserCreateWithOrganization dto, String lang) {
        validator.validateOnCreateWithOrganization(dto,lang);
        AuthUser user = mapper.toEntityFromCreateAuthUserWithOrganization(dto,lang);
        publisher.publishEvent(new SendEmailToAuthUser(this, user.getEmail(), "Hey there!\nYou have been successfully registered to Hub!🤗🤗🤗\nHere are details for login: username = %s and password = %s for login. DO NOT SHARE IT WITH ANYONE!!!".formatted(user.getUsername(), user.getPassword()), "Welcome to Hub.com"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return mapper.toDto(repository.save(user));
    }
}
