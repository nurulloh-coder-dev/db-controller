package com.example.service;

import org.springframework.stereotype.Service;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectDatabaseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VersionProviderService {

    private final ProjectDatabaseRepository projectDatabaseRepository;

    public VersionProviderService(ProjectDatabaseRepository projectDatabaseRepository) {
        this.projectDatabaseRepository = projectDatabaseRepository;
    }

    public Integer getMaxVersionAndIncrement(ProjectDatabase database) {
        Optional<Integer> maxVersion = projectDatabaseRepository.getMaxVersion(database.getId());
        Integer maxVersionInt = maxVersion.orElse(0);
        return maxVersionInt + 1;
    }

    public void updateUserToMaxVersions(String authId) {
        List<String> databaseIds = projectDatabaseRepository.getDatabaseIds(authId);
        for (String databaseId : databaseIds) {
            projectDatabaseRepository.updateVersion(databaseId,authId);
        }
    }
}
