package com.example.repository;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.DatabaseRole;
import com.example.model.entity.ProjectDatabase;
import com.example.model.entity.ProjectDatabaseUser;

import java.util.List;
import java.util.Optional;

public interface ProjectDatabaseRepository extends JpaRepository<ProjectDatabase, String> {

    @Query(value = "SELECT * FROM project_database_user WHERE id IN (:ids) AND deleted = false",
            nativeQuery = true)
    List<ProjectDatabaseUser> findAllByIdIn(@Param("ids") List<Long> membersId);

    Optional<ProjectDatabase> findByIdAndDeleted(String id, Boolean deleted);

    @Query(value = "select * from project_database where deleted = false and organization_id = (select organization_id from auth_user where id = :authUserId)", nativeQuery = true)
    List<ProjectDatabase> findAllByAuthIdAndOrganizationId(@Param("authUserId") String authUserId);

    Optional<ProjectDatabase> findByAgentId(String agentId);

    @Query(value = "select * from database_role where database_id = :id and deleted = false order by name", nativeQuery = true)
    List<DatabaseRole> findAllByDatabaseId(@Param("id") String id);

    @Query(value = "select max(version) from project_database_user where database_id = :databaseId", nativeQuery = true)
    Optional<Integer> getMaxVersion(@Param("databaseId") String id);

    @Query(value = "select project_database_id from project_database_members where members_id in(select id from project_database_user where deleted=false and user_id =:userId)", nativeQuery = true)
    List<String> getDatabaseIds(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "update project_database_user set version=1+ (select max(version) from project_database_user where database_id = :databaseId) where user_id =:userId and database_id =:databaseId and deleted = false", nativeQuery = true)
    void updateVersion(@Param("databaseId") String databaseId, @Param("userId") String authId);
}
