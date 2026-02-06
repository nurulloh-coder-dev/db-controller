package com.example.controller.active_users;

import java.time.Instant;

public interface ActiveUserProjection {
    String getUserId();
    String getUserName();
    String getEmail();
    String getOrganizationId();
    String getOrganizationName();
    Instant getSessionStart();
    Instant getLastActivity();
}