package com.example.validator;


import com.example.exception.ObjectNotFound;
import com.example.exception.UserAlreadyExist;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.config.CustomUserDetails;
import com.example.model.dto.authUser.AuthUserCreateDto;
import com.example.model.dto.authUser.AuthUserUpdateDto;
import com.example.model.dto.special.AuthUserCreateWithOrganization;
import com.example.model.entity.AuthUser;
import com.example.model.entity.enums.WebLang;
import com.example.repository.AuthUserRepository;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.util.Utils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUserValidator implements BaseValidator {
    private final AuthUserRepository repository;
    private final ProjectDatabaseUserRepository projectDatabaseUserRepository;
    private final MessageSource messageSource;
    private final Utils utils;

    public void validateOnCreate(AuthUserCreateDto dto) {
        Optional<AuthUser> optionalRes =
                repository.findByUsernameAndDeletedFalse(dto.getUsername());
        if (optionalRes.isPresent()) {
            throw new UserAlreadyExist(messageSource.getMessage("username.exists",null,utils.getLocaleByLanguage(optionalRes.get().getSettings().getLanguage())));
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
    }

    public AuthUser existsAndGet(String id) {
        return repository.findByIdAndDeleted(id, false).orElseThrow(
                () -> new ObjectNotFound("User with id " + id + " not found")
        );
    }


    public String validateAuthenticationAndGetId() {
        Authentication authentication = checkAuthentification();

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        if (principal.getUserId() == null) {
            throw new RuntimeException("User ID missing");
        }

        return principal.getUserId();
    }

    public WebLang validateAuthenticationAndGetLanguage() {
        Authentication authentication = checkAuthentification();

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        if (principal.getUserId() == null) {
            throw new RuntimeException("User ID missing");
        }

        return principal.getWebLang();
    }

    public void deleteDatabaseMembership(String id) {
        projectDatabaseUserRepository.deleteMemberFromDatabaseByAuthUser(id);
    }

    public void validateOnCreateWithOrganization(AuthUserCreateWithOrganization dto, String lang) {
        Optional<AuthUser> optional = repository.findByUsernameAndDeletedFalse(dto.getUsername());

        if(optional.isPresent()) {
            throw new UserAlreadyExist(messageSource.getMessage("username.exists",null,utils.getLocaleByLanguage(lang)));
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new RuntimeException("full name is required");
        }

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }
    }

    public void validateOnUpdate(AuthUserUpdateDto dto, String id) {
        Optional<AuthUser> optional = repository.findByUsernameAndDeletedFalse(dto.getUsername());
        if (optional.isPresent()) {
            if (!optional.get().getId().equals(id)){
                throw new RuntimeException(messageSource.getMessage("username.exists",null,utils.getLocaleByLanguage(validateAuthenticationAndGetLanguage())));
            }
        }
    }

    public String validateAuthenticationAndGetOrganizationId() {
        Authentication authentication = checkAuthentification();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        if (principal.getUserId() == null) {
            throw new RuntimeException("organization ID missing");
        }
        return principal.getOrganizationId();
    }



    public String validateAuthenticationAndGetUsername() {
        Authentication authentication = checkAuthentification();
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getUsername();
    }


    private Authentication checkAuthentification() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication;
    }
}
