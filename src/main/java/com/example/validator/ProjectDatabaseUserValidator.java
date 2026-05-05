package com.example.validator;

import com.example.exception.ObjectNotFound;
import com.example.exception.UserAlreadyExist;
import com.example.repository.ProjectDatabaseRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
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

    @SneakyThrows
    public void validateOnCreate(ProjectDatabaseUserCreateDto createDto) {
        if (authUserValidator.validateIdAndGet(createDto.getAuthUserId()).getDbUsername()==null){
            throw new BadRequestException("database username cannot be null");
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
