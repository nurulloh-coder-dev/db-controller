package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.Organization;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization,String> {

    Optional<Organization> findByIdAndDeleted(String id, Boolean deleted);

    @Query(value = "select * from organization where id = (select organization_id from auth_user where id =:authUserId)",nativeQuery = true)
    Optional<Organization> findByUserId(@Param("authUserId") String authUserId);
}
