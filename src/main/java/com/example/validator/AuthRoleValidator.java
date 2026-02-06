package com.example.validator;

import com.example.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.authRole.AuthRoleChangeDto;
import com.example.model.entity.AuthRole;
import com.example.repository.AuthRoleRepository;

@Component
@RequiredArgsConstructor
public class AuthRoleValidator implements BaseValidator {

    private final AuthRoleRepository repository;
    public AuthRole existsAndGet(String roleId) {
        return repository.findById(roleId).orElseThrow(
                () -> new ObjectNotFound("Role with id " + roleId + " not found")
        );
    }

    public void validateOnCreate(AuthRoleChangeDto dto) {
        if (dto.getName() == null||dto.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }
        if (dto.getCode() == null||dto.getCode().isBlank()) {
            throw new RuntimeException("Code is required");
        }
    }
}
