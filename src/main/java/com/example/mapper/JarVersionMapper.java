package com.example.mapper;

import org.springframework.stereotype.Component;
import com.example.model.dto.jar.JarVersionDto;
import com.example.model.dto.jar.JarVersionCreateDto;
import com.example.model.dto.jar.JarVersionUpdateDto;
import com.example.model.entity.JarVersion;
import com.example.service.FileService;

import java.util.List;

@Component
public class JarVersionMapper implements BaseMapper{
    private final FileService fileService;

    public JarVersionMapper(FileService fileService) {
        this.fileService = fileService;
    }

    public JarVersionDto toDto(JarVersion jarVersion) {
        JarVersionDto jarVersionDto = new JarVersionDto();
        jarVersionDto.setVersion(jarVersion.getVersion());
        jarVersionDto.setDescription(jarVersion.getDescription());
        jarVersionDto.setId(jarVersion.getId());
        jarVersionDto.setReleaseDate(jarVersion.getReleaseDate());
        jarVersionDto.setSizeInBytes(jarVersion.getSizeInBytes());
        return  jarVersionDto;
    }

    public JarVersion toEntity(JarVersionCreateDto dto) {
        return fileService.getAgentVersion(dto);
    }

    public JarVersion toEntity(JarVersionUpdateDto dto) {
        JarVersion jarVersion = new JarVersion();


        return  jarVersion;
    }

    public List<JarVersionDto> toDtoList(List<JarVersion> allByDeletedFalse) {
        return allByDeletedFalse
                .stream()
                .map(this::toDto)
                .toList();

    }
}
