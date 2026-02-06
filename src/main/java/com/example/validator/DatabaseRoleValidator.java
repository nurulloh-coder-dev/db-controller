package com.example.validator;

import com.example.exception.ObjectNotFound;
import org.springframework.stereotype.Component;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.entity.DatabaseRole;
import com.example.repository.DatabaseRoleRepository;

@Component
public record DatabaseRoleValidator(
        DatabaseRoleRepository repository
) implements BaseValidator{

    public void validateOnCreate(DatabaseRoleCreateDTO dto) {
        //validate logic
    }

    public DatabaseRole existsAndGet(String id) {
        return repository.findById(id).orElseThrow(
                () -> new ObjectNotFound("Database role with id " + id + " not found")
        );
    }
}
