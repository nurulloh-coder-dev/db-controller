package com.example.repository;

import com.example.model.dto.special.AgentDbUserRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.ProjectDatabaseUser;

import java.util.List;
import java.util.Optional;

public interface DatabaseUserRepository extends CrudRepository<ProjectDatabaseUser, String> {


    @Query(value = """
            select
                du.id as id,
                du.username as username,
                au.db_password as dbPassword,
                du.deleted as deleted,
                du.version as version,
                pd.name as databaseName,
                COALESCE(
                    json_agg(DISTINCT dr.code) FILTER (WHERE dr.code IS NOT NULL),
                    '[]'::json
                )::text as roles
            from project_database_user du
                left join auth_user au on du.user_id = au.id
                left join project_database pd on du.database_id = pd.id
                left join database_user_role ur on du.id = ur.database_user_id
                left join database_role dr on dr.id = ur.database_role_id
            where du.version > ?2
              and pd.agent_id = ?1
            group by du.id, du.username, au.db_password, du.deleted, du.version, pd.name
            order by du.version asc
            """, nativeQuery = true)
    List<AgentDbUserRow> getAllForAgent(String agentId, Long version);


    @Query(value = "select max(version) from project_database_user where database_id = (select id from project_database where agent_id = :agentId and deleted = false)", nativeQuery = true)
    Optional<Integer> getMaxVersion(@Param("agentId") String agentId);


    @Query(value = """
            select database_url from project_agent where id in 
                   (select agent_id from project_database where id in 
                         (select project_database_id from project_database_user where members_id =:userId))
            """, nativeQuery = true)
    List<String> getUserDatabases(@Param("userId") String userId);
}
