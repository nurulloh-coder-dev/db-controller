package com.example.validator;

import com.example.exception.ObjectNotFound;
import com.example.exception.UserAlreadyExist;
import com.example.repository.ProjectDatabaseRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.entity.ProjectDatabaseUser;
import com.example.repository.ProjectDatabaseUserRepository;
import com.example.util.Utils;

import java.util.Optional;

@Component
@AllArgsConstructor

public class ProjectDatabaseUserValidator implements BaseValidator {
    private final ProjectDatabaseUserRepository repository;
    private final AuthUserValidator authUserValidator;
    private final MessageSource messageSource;
    private final Utils utils;
    private final ProjectDatabaseRepository projectDatabaseRepository;

    public void validateOnCreate(ProjectDatabaseUserCreateDto createDto) {
        if (createDto.getDbUsername() == null || createDto.getDbUsername().isBlank()) {
            throw new RuntimeException("username is required");
        }
//        if (createDto.getDbPassword() == null || createDto.getDbPassword().isBlank()) {
//            throw new RuntimeException("password is required");
//        }
        if (createDto.getDbUsername().trim().contains(" ")){
            throw new RuntimeException(messageSource.getMessage("username.space.conflict",null,utils.getLocaleByLanguage(authUserValidator.validateAuthenticationAndGetLanguage())));
        }
        Optional<Boolean> optionalRes =
                repository.checkUsernameConflictWithRole(createDto.getDbUsername());
        if (optionalRes.isPresent()) {
            throw new RuntimeException(messageSource.getMessage("username.conflict.role",null,utils.getLocaleByLanguage(authUserValidator.validateAuthenticationAndGetLanguage())));
        }

        Optional<Boolean> usernameCheckRes =
                repository.checkUsernameUniqueness(createDto.getDatabaseId(),createDto.getDbUsername());

        if (usernameCheckRes.isPresent()) {
            throw new UserAlreadyExist(messageSource.getMessage("username.exists.db",null,utils.getLocaleByLanguage(authUserValidator.validateAuthenticationAndGetLanguage())));
        }
    }

    public ProjectDatabaseUser validateId(String id) {
       return repository.findByIdAndDeleted(id,false)
                .orElseThrow(() -> new ObjectNotFound("member not found"));
    }

    public String validateAuthUserId(){
        return authUserValidator.validateAuthenticationAndGetId();
    }

}
