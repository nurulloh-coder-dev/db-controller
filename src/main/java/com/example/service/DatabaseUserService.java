package com.example.service;

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
        List<Object[]> users = repository.getAllForAgent(agentId, version);

        users.forEach(user -> {
            String rolesJson = (String) user[6];
            List<String> rolesList = Collections.emptyList();

            if (rolesJson != null && !rolesJson.equals("null") && !rolesJson.trim().isEmpty()) {
                try {
                    rolesList = mapper.readValue(rolesJson, new TypeReference<>() {
                    });
                } catch (JsonProcessingException ignored) {
                }
            }

            resp.add(UserDto.builder()
                    .id(user[0].toString())
                    .username(user[1].toString())
                    .password(user[2]!=null?user[2].toString():null)
                    .deleted(Boolean.parseBoolean(user[3].toString()))
                    .version(user[4] instanceof Number ? ((Number) user[4]).intValue() : Integer.parseInt(user[4].toString()))
                    .dbName(user[5].toString())
                    .roles(rolesList == null ? Collections.emptyList() : rolesList)
                    .build());
        });
        return resp;
    }
}