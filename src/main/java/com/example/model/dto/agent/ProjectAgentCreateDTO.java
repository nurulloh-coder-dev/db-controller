package com.example.model.dto.agent;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectAgentCreateDTO {
    private String id;
    private String name;
    @JsonAlias("username")
    private String databaseUsername;
    @JsonAlias("password")
    private String databasePassword;
    @JsonAlias("dbUrl")
    private String databaseUrl;
    private String organizationId;
    private Integer version;
}
