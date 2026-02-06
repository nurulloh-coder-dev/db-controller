package com.example.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.DatabaseRole;
import com.example.model.entity.ProjectDatabase;
import com.example.model.entity.ProjectDatabaseUser;

import java.util.List;
import java.util.Optional;

public interface ProjectDatabaseRepository extends JpaRepository<ProjectDatabase, String> {

    @Query(value = "select * from project_database where name = :name and deleted =  false limit 1",
            nativeQuery = true)
    Optional<ProjectDatabase> findDbByName(@Param("name") String name);

    @Query(value = "SELECT * FROM project_database_user WHERE id IN (:ids) AND deleted = false",
            nativeQuery = true)
    List<ProjectDatabaseUser> findAllByIdIn(@Param("ids") List<Long> membersId);

    Optional<ProjectDatabase> findByIdAndDeleted(String id, Boolean deleted);

    @Query(value = "select * from project_database where deleted = false and organization_id = (select organization_id from auth_user where id = :authUserId)",nativeQuery = true)
    List<ProjectDatabase> findAllByAuthIdAndOrganizationId(@Param("authUserId") String authUserId);

    Optional<ProjectDatabase> findByAgentId(String agentId);

    @Query(value = "select * from database_role where database_id = :id and deleted = false order by name",nativeQuery = true)
    List<DatabaseRole> findAllByDatabaseId(@Param("id") String id);

    @Query(value = "select max(version) from project_database_user where database_id = :databaseId",nativeQuery = true)
    Optional<Integer> getMaxVersion(@Param("databaseId") String id);

    @Query(value = "select 1 from project_database_user where username = :username and database_id= :dbId",nativeQuery =true)
    Optional<Integer> findByUsername(@Param("username") String dbUsername, @Param("dbId") String databaseId);

}
