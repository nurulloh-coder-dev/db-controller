package com.example.model.dto.authUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.model.dto.organization.OrganizationDto;
import com.example.model.dto.special.IdNameDto;
import com.example.model.entity.WebSettings;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {
    private String id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private IdNameDto role;
    private WebSettings settings;
    private OrganizationDto organization;
}
