package com.example.validator;

import com.example.exception.ObjectNotFound;
import com.example.model.entity.AuthUser;
import com.example.model.entity.ProjectAgent;
import com.example.model.entity.ProjectDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Tenant isolation checks: every record a user touches must belong to the user's own organization.
 * Records from another organization are reported as "not found" so their existence is not leaked.
 */
@Component
@RequiredArgsConstructor
public class AccessValidator {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final AuthUserValidator authUserValidator;

    public String currentOrganizationId() {
        return authUserValidator.validateAuthenticationAndGetOrganizationId();
    }

    public String currentUserId() {
        return authUserValidator.validateAuthenticationAndGetId();
    }

    public boolean isAdmin() {
        return hasAuthority(ROLE_ADMIN);
    }

    public boolean isSuperAdmin() {
        return hasAuthority(ROLE_SUPER_ADMIN);
    }

    public void checkOrganization(String organizationId) {
        if (organizationId == null || !organizationId.equals(currentOrganizationId())) {
            throw new ObjectNotFound("Resource not found");
        }
    }

    public ProjectDatabase checkDatabase(ProjectDatabase database) {
        checkOrganization(database.getOrganization() == null ? null : database.getOrganization().getId());
        return database;
    }

    public AuthUser checkUser(AuthUser authUser) {
        checkOrganization(authUser.getOrganization() == null ? null : authUser.getOrganization().getId());
        return authUser;
    }

    public ProjectAgent checkAgent(ProjectAgent agent) {
        checkOrganization(agent.getOrganizationId());
        return agent;
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Admin role required");
        }
    }

    private boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }
}
