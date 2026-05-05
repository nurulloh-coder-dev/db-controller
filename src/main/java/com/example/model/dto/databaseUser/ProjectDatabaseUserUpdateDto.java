package com.example.model.dto.databaseUser;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDatabaseUserUpdateDto {
    private String dbUsername;
    private String databaseId;
    private String authUserId;

    @JsonAlias({"roles"})
    private List<String> roleIds;
}
