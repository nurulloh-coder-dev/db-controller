package com.example.validator;

import com.example.exception.ObjectNotFound;
import org.springframework.stereotype.Component;
import com.example.model.dto.organization.OrganizationCreateDto;
import com.example.model.dto.organization.OrganizationUpdateDto;
import com.example.model.entity.Organization;
import com.example.repository.OrganizationRepository;

@Component
public class OrganizationValidator implements BaseValidator {
    private final OrganizationRepository repository;
    private final AuthUserValidator authUserValidator;

    public OrganizationValidator(OrganizationRepository repository, AuthUserValidator authUserValidator) {
        this.repository = repository;
        this.authUserValidator = authUserValidator;
    }

    public Organization validateId(String id) {
        return repository.findByIdAndDeleted(id, false)
                .orElseThrow(() -> new ObjectNotFound("Organization Not Found"));
    }

    public Organization validateOrganizationByUserId() {
        String authUserId = authUserValidator.validateAuthenticationAndGetId();
        return repository.findByUserId(authUserId)
                .orElseThrow(() -> new ObjectNotFound("Organization Not Found"));
    }

    public void validateOnCreate(OrganizationCreateDto dto) {
        if (dto.getOrganizationName().isBlank()) {
            throw new RuntimeException("organization name should not be empty");
        }
    }

    public void validateOnUpdate(OrganizationUpdateDto dto) {
        if (dto.getOrganizationName().isBlank()) {
            throw new RuntimeException("organization name should not be empty");
        }
    }
}
