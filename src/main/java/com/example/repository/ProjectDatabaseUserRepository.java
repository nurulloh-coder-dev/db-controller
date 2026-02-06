package com.example.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import com.example.model.entity.ProjectDatabase;
import com.example.model.entity.ProjectDatabaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ProjectDatabaseUserRepository extends JpaRepository<ProjectDatabaseUser, String> {

    @Query(value = "select * from project_database where id in (select database_id from project_database_user where user_id =:memberId and deleted = false)", nativeQuery = true)
    List<ProjectDatabase> findAllByMemberId(@Param("memberId") String memberId);

    Optional<ProjectDatabaseUser> findByIdAndDeleted(String id, Boolean deleted);

    @Transactional
    @Modifying
    @Query(value = "delete from project_database_members where members_id in (select id from project_database_user where user_id = :authUserId and deleted = false) ", nativeQuery = true)
    void deleteMemberFromDatabaseByAuthUser(@Param("authUserId") String authUserId);

    @Transactional
    @Modifying
    @Query(value = "update project_database_user set deleted = true where user_id =:authUserId", nativeQuery = true)
    void deleteMemberByAuthUser(@Param("authUserId") String authUserId);

    @Query(value = "select * from project_database_user where deleted = false and user_id = :userId", nativeQuery = true)
    List<ProjectDatabaseUser> getAllByAuthUser(@Param("userId") String id);

    @Query(value = "select true from database_role where name = :dbUsername and deleted = false", nativeQuery = true)
    Optional<Boolean> checkUsernameConflictWithRole(@Param("dbUsername") String dbUsername);


    @Modifying
    @Transactional
    @Query(value = "delete from project_database_members where project_database_id= :databaseId and members_id = :memberId", nativeQuery = true)
    void deleteMemberFromDatabase(@Param("databaseId") String databaseId, @Param("memberId") String memberId);

    @Query(value = "select true from project_database_user where database_id =:dbId and username =:username and deleted = false", nativeQuery = true)
    Optional<Boolean> checkUsernameUniqueness(@Param("dbId") String dbId, @Param("username") String username);

    @Modifying
    @Transactional
    @Query(value = "delete from project_database_members where project_database_id= :dbId and members_id in (:members)",nativeQuery = true)
    void deleteMembersFromArray(@Param("dbId") String id, @Param("members") List<String> removedUsers);

    @Modifying
    @Transactional
    @Query(value = "update  project_database_user set deleted = true where project_database_id= :dbId and members_id in (:members)",nativeQuery = true)
    void removeMembers(String id, List<String> removedUsers);

    @Query(value = "select * from project_database_user where database_id = :dbId and username= :username and deleted = false",nativeQuery = true)
    Optional<ProjectDatabaseUser> findByUsernameAndDatabaseId(@Param("username") String username, @Param("dbId") String databaseId);
}
