package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.model.entity.DatabaseRole;

import java.util.Collection;
import java.util.List;

@Repository
public interface DatabaseRoleRepository extends JpaRepository<DatabaseRole, String> {
    List<DatabaseRole> findAllByIdIn(Collection<String> ids);

    @Query(value = "select * from database_role where database_id =:databaseId and deleted = false",nativeQuery = true)
    List<DatabaseRole> findAllByDatabaseId(@Param("databaseId") String databaseId);

    @Query(value = "select * from database_role where database_id = :dbId and name in :names",nativeQuery = true)
    List<DatabaseRole> getAllByName(@Param("dbId")String databaseId, @Param("names") List<String> name);
}
