package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectDatabaseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VersionProviderService {

    private final ProjectDatabaseRepository projectDatabaseRepository;
    private final AgentEventService agentEventService;

    public VersionProviderService(ProjectDatabaseRepository projectDatabaseRepository, AgentEventService agentEventService) {
        this.projectDatabaseRepository = projectDatabaseRepository;
        this.agentEventService = agentEventService;
    }

    // every change an agent has to apply gets a new version here, so this is where agents are told
    public Integer getMaxVersionAndIncrement(ProjectDatabase database) {
        Optional<Integer> maxVersion = projectDatabaseRepository.getMaxVersion(database.getId());
        Integer maxVersionInt = maxVersion.orElse(0);
        notifyAgent(database);
        return maxVersionInt + 1;
    }

    public void updateUserToMaxVersions(String authId) {
        List<String> databaseIds = projectDatabaseRepository.getDatabaseIds(authId);
        for (String databaseId : databaseIds) {
            projectDatabaseRepository.updateVersion(databaseId,authId);
            projectDatabaseRepository.findById(databaseId).ifPresent(this::notifyAgent);
        }
    }

    private void notifyAgent(ProjectDatabase database) {
        if (database.getAgent() == null) {
            return;
        }
        String agentId = database.getAgent().getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // tell the agent only once the change is committed, or it would fetch the old state
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    agentEventService.notifyAgent(agentId);
                }
            });
        } else {
            agentEventService.notifyAgent(agentId);
        }
    }
}
