package com.example.mapper;

import com.example.model.entity.*;
import com.example.validator.ProjectDatabaseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.authUser.AuthUserCreateDto;
import com.example.model.dto.authUser.AuthUserDto;
import com.example.model.dto.authUser.AuthUserUpdateDto;
import com.example.model.dto.organization.OrganizationDto;
import com.example.model.dto.special.AuthUserCreateWithOrganization;
import com.example.model.dto.special.IdNameDto;
import com.example.model.entity.enums.WebLang;
import com.example.model.entity.enums.WebTheme;
import com.example.repository.AuthRoleRepository;
import com.example.repository.AuthUserRepository;
import com.example.repository.WebSettingsRepository;
import com.example.service.PasswordGenerator;
import com.example.validator.AuthRoleValidator;
import com.example.validator.AuthUserValidator;
import com.example.validator.OrganizationValidator;


import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class AuthUserMapper implements BaseMapper {
    private final AuthRoleValidator authRoleValidator;
    private final PasswordGenerator passwordGenerator;
    private final OrganizationValidator organizationValidator;
    private final AuthUserValidator authUserValidator;
    private final AuthUserRepository authUserRepository;
    private final WebSettingsRepository webSettingsRepository;
    private final AuthRoleRepository authRoleRepository;
    private final OrganizationMapper organizationMapper;
    private final ProjectDatabaseValidator projectDatabaseValidator;

    public AuthUser fromDto(AuthUserCreateDto dto) {
        AuthRole role = authRoleValidator.existsAndGet(dto.getRoleId());
        AuthUser authUser = new AuthUser();
        authUser.setEmail(dto.getEmail());
        authUser.setUsername(dto.getUsername());
        authUser.setPassword(passwordGenerator.generatePassword());
        authUser.setName(dto.getName());
        authUser.setPhone(dto.getPhone());
        authUser.setRole(role);
        Organization organization = organizationValidator.validateOrganizationByUserId();
        authUser.setCreatedBy(authUserValidator.validateAuthenticationAndGetId());
        authUser.setSettings(webSettingsRepository.save(new WebSettings(WebTheme.AUTO, WebLang.ENG, true)));
        authUser.setOrganization(organization);
        authUser.setDbPassword(passwordGenerator.generateUUID());
        authUser.setDbUsername(passwordGenerator.generateUUID());
        return authUser;
    }

    public void fromDto(AuthUserUpdateDto dto, AuthUser authUser) {
        AuthRole role = authRoleValidator.existsAndGet(dto.getRoleId());
        authUser.setEmail(dto.getEmail());
        authUser.setUsername(dto.getUsername());
        authUser.setName(dto.getName());
        authUser.setPhone(dto.getPhone());
        authUser.setUpdatedBy(authUserValidator.validateAuthenticationAndGetId());
        authUser.setRole(role);
    }

    public void fromDtoIgnoreNull(AuthUserUpdateDto dto, AuthUser authUser) {
        if (dto.getEmail() != null) {
            authUser.setEmail(dto.getEmail());
        }
        if (dto.getUsername() != null) {
            Optional<AuthUser> optional = authUserRepository.findByUsernameAndDeletedFalse(dto.getUsername());
            if (optional.isPresent()) {
                throw new RuntimeException("user name is already in use. Please choose another username!");
            }
            authUser.setUsername(dto.getUsername());
        }
        if (dto.getName() != null) {
            authUser.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            authUser.setPhone(dto.getPhone());
        }
        if (dto.getRoleId() != null) {
            authUser.setRole(authRoleValidator.existsAndGet(dto.getRoleId()));
        }
    }

    public AuthUserDto toDto(AuthUser authUser) {
        AuthRole authRole = authUser.getRole();
        IdNameDto role = authRole == null ? null : IdNameDto.builder()
                .id(authRole.getId())
                .name(authRole.getName())
                .build();

        WebSettings webSettings = webSettingsRepository.findById(authUser.getSettings().getId()).orElse(null);

        OrganizationDto organization = organizationMapper.toDto(organizationValidator.validateId(authUser.getOrganization().getId()));
        return AuthUserDto.builder()
                .username(authUser.getUsername())
                .id(authUser.getId())
                .email(authUser.getEmail())
                .organization(organization)
                .name(authUser.getName())
                .phone(authUser.getPhone())
                .role(role)
                .settings(webSettings)
                .build();
    }

    public List<AuthUserDto> toDto(List<AuthUser> authUsers) {
        return authUsers
                .stream()
                .map(this::toDto)
                .toList();
    }


    public List<AuthUser> findAllByNameLike(String search) {
        return authUserRepository.findAllBySearch(search, authUserValidator.validateAuthenticationAndGetId());
    }

    public List<AuthUser> findAllByNameLike(String search, String dbId) {
        List<AuthUser> list = projectDatabaseValidator.validateId(dbId).getMembers().stream().map(ProjectDatabaseUser::getAuthUser).toList();
        List<AuthUser> allBySearch = authUserRepository.findAllBySearch(search, authUserValidator.validateAuthenticationAndGetId());
        return allBySearch
                .stream()
                .filter(u -> !list.contains(u))
                .toList();
    }

    public List<AuthUser> findAllByCompany() {
        String organizationId = authUserValidator.validateAuthenticationAndGetOrganizationId();
        return authUserRepository.findAllByOrganization(organizationId);
    }

    public void mapSettingsPatch(AuthUser authUser, WebSettings settings) {
        if (settings.getLanguage() != null) {
            authUser.getSettings().setLanguage(settings.getLanguage());
        }
        if (settings.getTheme() != null) {
            authUser.getSettings().setTheme(settings.getTheme());
        }
        if (settings.getEnableEmailing() != null) {
            authUser.getSettings().setEnableEmailing(settings.getEnableEmailing());
        }
    }

    public AuthUser toEntityFromCreateAuthUserWithOrganization(AuthUserCreateWithOrganization dto, String lang) {
        AuthUser authUser = new AuthUser();
        authUser.setName(dto.getFullName());
        authUser.setUsername(dto.getUsername());
        authUser.setOrganization(organizationValidator.validateId(dto.getOrganizationId()));
        Optional<AuthRole> admin = authRoleRepository.getAdmin();
        if (admin.isEmpty()) {
            throw new RuntimeException("admin role is not found");
        }
        authUser.setRole(admin.get());
        authUser.setPassword(passwordGenerator.generatePassword());
        authUser.setEmail(dto.getEmail());
        WebSettings save = webSettingsRepository.save(new WebSettings(WebTheme.AUTO, WebLang.valueOf(lang), true));
        authUser.setSettings(save);
        authUser.setDbPassword(passwordGenerator.generateUUID());
        authUser.setDbUsername(passwordGenerator.generateUUID());
        return authUser;
    }
}
