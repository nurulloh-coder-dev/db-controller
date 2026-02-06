package com.example.repository;

import com.example.model.entity.JarVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JarVersionRepository extends JpaRepository<JarVersion, String> {
    @Query(value = "select * from jar_version where deleted = false",nativeQuery = true)
    List<JarVersion> findAllByDeletedFalse();
}
