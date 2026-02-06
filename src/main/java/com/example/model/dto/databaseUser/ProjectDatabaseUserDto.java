package com.example.model.dto.databaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import com.example.model.dto.authUser.AuthUserDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDatabaseUserDto {
    private String id;
    private String dbUsername;
    private String dbPassword;
    private AuthUserDto authUserDto;
    private String databaseId;

    private List<DatabaseRoleDTO> roles;
}
