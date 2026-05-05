package com.example.service;

import com.example.model.dto.special.AgentDbUserRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.model.dto.special.UserDto;
import com.example.repository.DatabaseUserRepository;

import java.util.*;

@Slf4j
@Service
public class DatabaseUserService {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DatabaseUserRepository repository;

    public DatabaseUserService(DatabaseUserRepository repository) {
        this.repository = repository;
    }


    public List<UserDto> getAll(String agentId, Long version) {
        List<UserDto> resp = new ArrayList<>();
        Optional<Integer> maxVersion = repository.getMaxVersion(agentId);
        if (maxVersion.isEmpty() || maxVersion.get() <= version) {
            return resp;
        }
        List<AgentDbUserRow> users = repository.getAllForAgent(agentId, version);

        users.forEach(user -> {
            String rolesJson = user.getRoles();
            List<String> rolesList = Collections.emptyList();

            if (rolesJson != null && !rolesJson.equals("null") && !rolesJson.trim().isEmpty()) {
                try {
                    rolesList = mapper.readValue(rolesJson, new TypeReference<>() {
                    });
                } catch (JsonProcessingException ignored) {
                }
            }

            resp.add(UserDto.builder()
                    .username(user.getUsername())
                    .password(user.getDbPassword()!=null? user.getDbPassword() : null)
                    .deleted(user.getDeleted())
                    .version(user.getVersion())
                    .dbName(user.getDatabaseName())
                    .roles(rolesList == null ? Collections.emptyList() : rolesList)
                    .build());
        });
        return resp;
    }
}