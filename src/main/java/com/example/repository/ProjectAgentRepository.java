package com.example.repository;

import com.example.model.entity.ProjectDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.model.entity.ProjectAgent;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAgentRepository extends JpaRepository<ProjectAgent, String> {
    Optional<ProjectAgent> findByDatabaseUrl(String databaseUrl);

    Optional<ProjectAgent> findByIdAndDeleted(String id, boolean deleted);

    @Query(value = "select 1 from project_agent where id =:id and deleted = false",nativeQuery = true)
    Optional<Integer> checkId(@Param("id") String id);

    @Query(value = "select * from project_database where agent_id =:agentId and deleted = false",nativeQuery = true)
    Optional<ProjectDatabase> getDatabaseIdByAgentId(@Param("agentId") String agentId);

    @Query(value = "select * from project_agent where name =:dbName and organization_id = :orgId and deleted = false",nativeQuery = true)
    Optional<ProjectAgent> findByDatabaseNameAndCompanyId(@Param("dbName") String dbName, @Param("orgId") String organizationId);

    @Query(value = "select username from project_database_user where deleted = false and database_id =:dbId and username is not null",nativeQuery = true)
    List<String> findUsernamesByDatabaseId(@Param("dbId") String databaseId);
}
