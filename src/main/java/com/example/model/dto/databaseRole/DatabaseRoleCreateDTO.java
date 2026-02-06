package com.example.model.dto.databaseRole;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class DatabaseRoleCreateDTO {
    private String roleName;
    private String description;
}
