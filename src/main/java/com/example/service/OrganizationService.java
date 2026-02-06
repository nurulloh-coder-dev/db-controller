package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.mapper.OrganizationMapper;
import com.example.model.dto.organization.OrganizationCreateDto;
import com.example.model.dto.organization.OrganizationDto;
import com.example.model.dto.organization.OrganizationUpdateDto;
import com.example.model.entity.Organization;
import com.example.repository.OrganizationRepository;
import com.example.validator.OrganizationValidator;

import java.util.List;

@Service
public class OrganizationService extends AbstractService<
        OrganizationRepository,
        OrganizationMapper,
        OrganizationValidator> implements CRUDService<OrganizationDto, OrganizationCreateDto, OrganizationUpdateDto,String>{

    public OrganizationService(OrganizationRepository repository, OrganizationMapper mapper, OrganizationValidator validator) {
        super(repository, mapper, validator);
    }

    @Override
    public OrganizationDto create(@RequestBody OrganizationCreateDto dto) {
        validator.validateOnCreate(dto);
        Organization organization = mapper.mapToEntityOnCreate(dto);
        return mapper.toDto(repository.save(organization));
    }

    public OrganizationDto create(@RequestBody OrganizationCreateDto dto,String lang) {
        validator.validateOnCreate(dto);
        Organization organization = mapper.mapToEntityOnCreate(dto);
        return mapper.toDto(repository.save(organization));
    }

    @Override
    public OrganizationDto update(String id, OrganizationUpdateDto dto) {
        validator.validateOnUpdate(dto);
        Organization organization = validator.validateId(id);
        mapper.mapUpdate(organization,dto);
        return mapper.toDto(repository.save(organization));
    }

    @Override
    public OrganizationDto get(String id) {
        Organization organization = validator.validateId(id);
        return mapper.toDto(organization);
    }

    @Override
    public List<OrganizationDto> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public void delete(String id) {
        Organization organization = validator.validateId(id);
        organization.setDeleted(true);
        repository.save(organization);
    }
}
