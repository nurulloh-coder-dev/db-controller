package com.example.service;

import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.example.model.dto.jar.JarVersionDto;
import com.example.model.entity.JarHistory;
import com.example.repository.JarDownloadRepository;
import com.example.validator.AuthUserValidator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JarDownloadService {
    private final FileService fileService;
    private final JarDownloadRepository repository;
    private final AuthUserValidator authUserValidator;

    public Optional<Resource> download(String name) {
        if (!name.startsWith("db-agent")) {
            name = ("db-agent-" +name);
        }
        Path jarPath = Paths.get(fileService.getDBAgent(name));
        if (!jarPath.toFile().exists()) {
            return Optional.empty();
        }
        Resource resource = new FileSystemResource(jarPath);
        String organizationId = authUserValidator.validateAuthenticationAndGetOrganizationId();
        String username = authUserValidator.validateAuthenticationAndGetUsername();
        repository.save(new JarHistory(name, organizationId, Instant.now(), username));
        return Optional.of(resource);
    }

    public List<JarVersionDto> getVersions() {
        return fileService.getAgentVersions();
    }


    public List<JarHistory> getHistory() {
        String organizationId = authUserValidator.validateAuthenticationAndGetOrganizationId();
        return repository.getAllByOrganizationId(organizationId);
    }
}
