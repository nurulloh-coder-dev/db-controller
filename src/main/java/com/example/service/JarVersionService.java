package com.example.service;

import org.springframework.stereotype.Service;
import com.example.mapper.JarVersionMapper;
import com.example.model.dto.jar.AdminJarVersionDto;
import com.example.model.dto.jar.JarVersionDto;
import com.example.model.dto.jar.JarVersionCreateDto;
import com.example.model.dto.jar.JarVersionUpdateDto;
import com.example.model.entity.JarVersion;
import com.example.repository.JarVersionRepository;
import com.example.validator.JarVersionValidator;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JarVersionService extends AbstractService<
        JarVersionRepository,
        JarVersionMapper,
        JarVersionValidator> implements CRUDService<JarVersionDto, JarVersionCreateDto, JarVersionUpdateDto, String> {


    private final JarDownloadService jarDownloadService;

    public JarVersionService(JarVersionRepository repository, JarVersionMapper mapper, JarVersionValidator validator, JarDownloadService jarDownloadService) {
        super(repository, mapper, validator);
        this.jarDownloadService = jarDownloadService;
    }

    @Override
    public JarVersionDto create(JarVersionCreateDto dto) {
        validator.validateOnCreate(dto);
        JarVersion save = repository.save(mapper.toEntity(dto));
        return mapper.toDto(save);
    }

    @Override
    public JarVersionDto update(String id, JarVersionUpdateDto dto) {
        validator.validateOnUpdate(id, dto);
        JarVersion entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public JarVersionDto get(String id) {
        JarVersion jarVersion = validator.validateId(id);
        return mapper.toDto(jarVersion);
    }

    @Override
    public List<JarVersionDto> getAll() {
        List<JarVersion> allByDeletedFalse = repository.findAllByDeletedFalse();
        return mapper.toDtoList(allByDeletedFalse);
    }

    @Override
    public void delete(String id) {
        JarVersion jarVersion = validator.validateId(id);
        jarVersion.setDeleted(true);
        repository.save(jarVersion);
    }

    public AdminJarVersionDto getAllForAdmin() {
        List<JarVersionDto> fileVersions = jarDownloadService.getVersions();
        List<JarVersionDto> allByDeletedFalse = mapper.toDtoList(repository.findAllByDeletedFalse());
        Set<String> approved = allByDeletedFalse
                .stream()
                .map(JarVersionDto::getVersion)
                .collect(Collectors.toSet());
        List<JarVersionDto> newVersions = fileVersions
                .stream()
                .filter((f) -> !approved.contains(f.getVersion()))
                .sorted(Comparator.comparing(JarVersionDto::getReleaseDate).reversed())
                .toList();

        return new AdminJarVersionDto(newVersions, allByDeletedFalse);
    }
}
