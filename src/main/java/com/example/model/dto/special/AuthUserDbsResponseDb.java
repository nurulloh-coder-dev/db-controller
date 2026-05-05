package com.example.model.dto.special;


import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;

@Data
@AllArgsConstructor
public class AuthUserDbsResponseDb {
    private String databaseName;
    private String DbUsername;
    private List<DatabaseRoleDTO> roleNames;
}
