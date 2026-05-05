package com.example.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.AuthUser;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, String> {
    Optional<AuthUser> findByUsernameAndDeletedFalse(String username);

    Optional<AuthUser> findByIdAndDeleted(String id, Boolean deleted);

    @Query(value = "select * from auth_user where deleted = false and organization_id = (select organization_id from auth_user where id = :authUserId) and (name ilike %:search% or username ilike %:search% or email ilike %:search%) ",nativeQuery = true)
    List<AuthUser> findAllBySearch(@Param("search") String search, @Param("authUserId") String authUserId);

    @Query(value = "select * from auth_user where deleted = false and organization_id = :organizationId",nativeQuery = true)
    List<AuthUser> findAllByOrganization(@Param("organizationId") String orgId);

    @Query(value = "select true from auth_user where id =:id and deleted = false",nativeQuery = true)
    Boolean checkIfExists(@Param("id") String id);


    @Modifying
    @Transactional
    @Query(value = "update auth_user set db_password =:password where deleted = false and id =:authId",nativeQuery = true)
    void updateDbPassword(@Param("authId") String authId, @Param("password") String password);

//    @Query(value = "select * from auth_user where deleted = false and (username =:username or email = :email)",nativeQuery = true)
//    Optional<AuthUser> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}
