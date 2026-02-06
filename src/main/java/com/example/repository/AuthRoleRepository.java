package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.model.entity.AuthRole;

import java.util.Optional;

public interface AuthRoleRepository extends JpaRepository<AuthRole, String> {

    @Query(value = "select * from auth_role where code = 'ADMIN' limit 1",nativeQuery = true)
    Optional<AuthRole> getAdmin();

    @Query(value = "select * from auth_role where code = 'DEVELOPER' limit 1",nativeQuery = true)
    Optional<AuthRole> getDeveloper();
}
