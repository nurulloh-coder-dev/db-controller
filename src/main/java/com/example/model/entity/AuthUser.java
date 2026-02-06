package com.example.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.BaseEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuthUser extends BaseEntity {
    private String name;
    private String username;
    private String password;

    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private AuthRole role;
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToOne
    private WebSettings settings;
}
