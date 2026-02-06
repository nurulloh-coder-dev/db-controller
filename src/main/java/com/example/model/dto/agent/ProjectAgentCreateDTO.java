package com.example.model.dto.agent;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectAgentCreateDTO {
    private String id;
    private String name;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private String organizationId;
    private Integer version;
}
