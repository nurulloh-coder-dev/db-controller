package com.example.model.dto.databaseUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDatabaseUserCreateDto {
    private String dbUsername;
    private String databaseId;
    private String authUserId;
    private List<String> roles = Collections.emptyList();
}