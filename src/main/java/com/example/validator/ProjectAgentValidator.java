package com.example.validator;

import com.example.exception.ObjectNotFound;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import org.springframework.stereotype.Component;
import com.example.model.dto.agent.ProjectAgentCreateDTO;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.entity.DatabaseRole;
import com.example.model.entity.Organization;
import com.example.model.entity.ProjectAgent;
import com.example.model.entity.ProjectDatabase;
import com.example.repository.ProjectAgentRepository;
import com.example.repository.ProjectDatabaseRepository;

import java.util.List;
import java.util.Optional;

@Component
public record ProjectAgentValidator(
        ProjectAgentRepository repository,
        ProjectDatabaseRepository databaseRepository,
        OrganizationValidator organizationValidator
) implements BaseValidator {

    public void validateOnCreate(ProjectAgentCreateDTO dto) {
        //validate logic
    }

    public ProjectAgent existsAndGet(String id) {
        return repository.findByIdAndDeleted(id, false).orElseThrow(
                () -> new ObjectNotFound("Project agent with id " + id + " not found")
        );
    }

//    public Optional<String> checkIfAlreadyExist(ProjectAgentCreateDTO dto) {
//        Optional<ProjectDatabase> dbByName = databaseRepository.findDbByName(dto.getName());
//        return dbByName.map(projectDatabase -> projectDatabase.getAgent().getId());
//    }

    public Organization validateOrganizationId(String organizationId) {
        return organizationValidator.validateId(organizationId);
    }

    public Boolean validateId(String agentId) {
        Optional<Integer> byIdAndDeleted = repository.checkId(agentId);
        return byIdAndDeleted.isPresent();
    }

    public void validateIfAnyRoleActive(ProjectDatabase database, List<DatabaseRoleCreateDTO> roles) {
        List<String> agentRoles = roles.stream()
                .map(DatabaseRoleCreateDTO::getRoleName)
                .toList();

        List<String> list = database.getMembers()
                .stream()
                .flatMap(member -> member.getRoles().stream())
                .map(DatabaseRole::getName)
                .toList();

        if (!list.isEmpty()) {
            list.forEach((role -> {
                if (!agentRoles.contains(role)) {
                    throw new RuntimeException("Role %s has some active reference. Please first remove the role from users that are using it".formatted(role));
                }
            }));
        }

    }

    public ProjectDatabase validateIdAndGetDatabaseId(String agentId) {
        Optional<ProjectDatabase> databaseIdByAgentId = repository.getDatabaseIdByAgentId(agentId);
        if (databaseIdByAgentId.isPresent()) {
            return  databaseIdByAgentId.get();
        }
        throw new ObjectNotFound("Agent not found");
    }


}
