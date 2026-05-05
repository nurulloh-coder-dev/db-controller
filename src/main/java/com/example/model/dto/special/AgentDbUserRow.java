package com.example.model.dto.special;

public interface AgentDbUserRow {
    String getUsername();
    String getDbPassword();
    Boolean getDeleted();
    Long getVersion();
    String getDatabaseName();
    String getRoles();
}
