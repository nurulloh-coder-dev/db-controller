package com.example.model.dto.special;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserCreateWithOrganization {
    private String organizationId;
    private String fullName;
    private String username;
    private String email;
}
