package com.example.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.model.entity.base.BaseEntity;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProjectDatabaseUser extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AuthUser authUser;

    private String username;
    private String password;

    @ManyToOne
    @JoinColumn(name = "database_id")
    private ProjectDatabase database;

    @ManyToMany
    @JoinTable(
            name = "database_user_role",
            joinColumns = @JoinColumn(name = "database_user_id"),
            inverseJoinColumns = @JoinColumn(name = "database_role_id")
    )
    private List<DatabaseRole> roles;

    private Integer version;
}


