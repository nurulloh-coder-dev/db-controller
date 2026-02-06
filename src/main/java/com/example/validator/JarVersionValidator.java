package com.example.validator;


import com.example.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.jar.JarVersionCreateDto;
import com.example.model.dto.jar.JarVersionUpdateDto;
import com.example.model.entity.JarVersion;
import com.example.repository.JarVersionRepository;
import com.example.service.FileService;

@Component
@AllArgsConstructor
public class JarVersionValidator implements BaseValidator {
    private final JarVersionRepository jarVersionRepository;
    private final FileService fileService;


    public JarVersion validateId(String id) {
        return jarVersionRepository.findById(id)
                .orElseThrow(()->new ObjectNotFound("agent version not found"));
    }

    public void validateOnCreate(JarVersionCreateDto dto) {
        fileService.validateAgentVersion(dto.getVersion());
    }

    public void validateOnUpdate(String id, JarVersionUpdateDto dto) {


    }
}
