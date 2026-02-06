package com.example.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.model.dto.jar.JarVersionDto;
import com.example.model.dto.jar.JarVersionCreateDto;
import com.example.model.entity.JarVersion;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private final Path userRegisterFile;
    private final Path agents;
    private final Path logs;

    public FileService(
            @Value("${app.user-register}") String userRegisterFilePath,
            @Value("${app.base-folder}") String agents,
            @Value("${app.logs-folder}") Path logs) {
        this.userRegisterFile = Path.of(userRegisterFilePath);
        this.agents = Path.of(agents);
        this.logs = logs;
    }

    @SneakyThrows
    public void writeUserRegister(String log) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(userRegisterFile.toFile(), true))) {
            bufferedWriter.write(log + ".\n");
        }
    }

    @SneakyThrows
    public void writeToFile(String log) {
        File file = new File(logs+ "\\logs\\app-" + LocalDate.now() + ".log");
        if (!file.exists()) {
            if (!file.createNewFile()) {
                return;
            }
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
            bufferedWriter.write(log + ". executed at " + LocalTime.now() + ".\n");
        }
    }

    @SneakyThrows
    public String readFile(LocalDate date) {
        StringBuilder res = new StringBuilder();
        String name = logs+ "\\logs\\app-" + LocalDate.now() + ".log";
        File file = new File(name);
        if (!file.exists()) {
            file = new File(logs+ "\\logs\\app-" + LocalDate.now().minusDays(1) + ".log");
        }
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while (bufferedReader.ready()) {
                res.append(bufferedReader.readLine()).append("\n");
            }
        }

        return res.toString();
    }

    @SneakyThrows
    public List<JarVersionDto> getAgentVersions() {
        File dir = new File(agents.toString());
        File[] files = dir.listFiles();
        List<JarVersionDto> fileNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (!f.isFile()) continue;
                Path p = f.toPath();
                BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                fileNames.add(new JarVersionDto(
                        f.getName().substring(9, f.getName().length() - 4),
                        Files.size(p),
                        attrs.creationTime().toInstant()));
            }
        }
        return fileNames;
    }

    public String getDBAgent(String name) {
        return agents+ "/" + name;
    }

    public void validateAgentVersion(String version) {
        String path  = agents+  "/db-agent-"+version+".jar";
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("agent with version %s not found".formatted(version));
        }
    }

    @SneakyThrows
    public JarVersion getAgentVersion(JarVersionCreateDto dto) {
        String path  = agents+ "/db-agent-"+dto.getVersion()+".jar";
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("agent with name %s not found".formatted(dto.getVersion()));
        }

        JarVersion jarVersion = new JarVersion();
        jarVersion.setVersion(dto.getVersion());
        jarVersion.setSizeInBytes(Files.size(file.toPath()));
        jarVersion.setReleaseDate(Instant.now());
        jarVersion.setDescription(dto.getDescription());
        return jarVersion;
    }
}
