package com.example.validator;

import com.example.exception.ObjectNotFound;
import com.example.exception.UserAlreadyExist;
import com.example.model.entity.AuthUser;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import com.example.model.dto.database.ProjectDatabaseCreateDto;
import com.example.model.entity.ProjectDatabase;
import com.example.model.entity.ProjectDatabaseUser;
import com.example.repository.ProjectDatabaseRepository;
import com.example.util.Utils;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ProjectDatabaseValidator implements BaseValidator {
    private final ProjectDatabaseRepository repository;
    private final AuthUserValidator authUserValidator;
    private final MessageSource messageSource;
    private final Utils utils;

    public ProjectDatabase validateId(String id) {
        return repository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new ObjectNotFound("Project database not found!"));
    }

    public void validateOnCreate(ProjectDatabaseCreateDto createDto) {
        if (createDto.getName() == null || createDto.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }
        if (createDto.getAgentId() == null || createDto.getAgentId().isBlank()) {
            throw new RuntimeException("Agent id cannot be empty");
        }
    }

    public void checkIfMemberAlreadyExists(ProjectDatabase database, String authUserId) {
        Optional<ProjectDatabaseUser> first = database.getMembers()
                .stream()
                .filter(m -> m.getAuthUser() != null && m.getAuthUser().getId().equals(authUserId) && !m.getDeleted())
                .findFirst();
        if (first.isPresent()) {
            throw new UserAlreadyExist(messageSource.getMessage("user.exists.db", null, utils.getLocaleByLanguage(authUserValidator.validateAuthenticationAndGetLanguage())));
        }

    }

    public String validateAuthUserId() {
        return authUserValidator.validateAuthenticationAndGetId();
    }

    public void checkIfAuthUserAlreadyExists(ProjectDatabase database, AuthUser authUser) {
        Optional<ProjectDatabaseUser> any = database.getMembers()
                .stream()
                .filter(m -> m.getAuthUser() != null && m.getAuthUser().getId().equals(authUser.getId()))
                .findAny();
        if (any.isPresent()) {
            throw new UserAlreadyExist(messageSource.getMessage("auth-user.already.exists", null, utils.getLocaleByLanguage(authUser.getSettings().getLanguage())));
        }
    }
}
