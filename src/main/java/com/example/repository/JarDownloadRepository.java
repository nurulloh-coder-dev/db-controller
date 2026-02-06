package com.example.repository;

import com.example.model.entity.JarHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JarDownloadRepository extends JpaRepository<JarHistory,String> {
    List<JarHistory> getAllByOrganizationId(String organizationId);
}
