package com.example.model.dto.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectAgentDTO {
    private String id;
    private String name;
    private String databaseUsername;
    private String databaseUrl;
    private Integer version;
    // only returned to the agent itself, right after registration
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String agentKey;
}
