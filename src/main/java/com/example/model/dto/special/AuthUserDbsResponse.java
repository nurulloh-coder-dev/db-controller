package com.example.model.dto.special;


import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;

@Data
@AllArgsConstructor
public class AuthUserDbsResponse {
    private String databaseName;
    private String memberId;
    private String username;
    private String password;
    private List<DatabaseRoleDTO> roleNames;
}
