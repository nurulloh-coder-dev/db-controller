package com.example.mapper;

import org.springframework.stereotype.Component;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import com.example.model.entity.DatabaseRole;

import java.util.List;

@Component
public class DatabaseRoleMapper implements BaseMapper {

    public DatabaseRole toEntityOnCreate(DatabaseRoleCreateDTO dto) {
        DatabaseRole databaseRole = new DatabaseRole();
        databaseRole.setName(dto.getRoleName());
        databaseRole.setDescription(dto.getDescription());
        return databaseRole;
    }

    public DatabaseRole toListDto(DatabaseRoleDTO dto) {
        DatabaseRole databaseRole = new DatabaseRole();
        databaseRole.setId(dto.getId());
        databaseRole.setName(dto.getName());
        databaseRole.setDescription(dto.getDescription());
        databaseRole.setCode(dto.getCode());
        return databaseRole;
    }

    public DatabaseRoleDTO toDto(DatabaseRole databaseRole) {
        DatabaseRoleDTO dto = new DatabaseRoleDTO();
        dto.setId(databaseRole.getId());
        dto.setName(databaseRole.getName());
        dto.setDescription(databaseRole.getDescription());
        dto.setCode(databaseRole.getCode());
        return dto;
    }
    public List<DatabaseRoleDTO> toListDto(List<DatabaseRole> roles) {
        return roles
                .stream()
                .map(this::toDto)
                .toList();
    }
}
