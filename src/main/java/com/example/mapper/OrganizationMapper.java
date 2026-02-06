package com.example.mapper;

import org.springframework.stereotype.Component;
import com.example.model.dto.organization.OrganizationCreateDto;
import com.example.model.dto.organization.OrganizationDto;
import com.example.model.dto.organization.OrganizationUpdateDto;
import com.example.model.entity.Organization;

import java.util.List;

@Component
public class OrganizationMapper implements BaseMapper {
    public Organization mapToEntityOnCreate(OrganizationCreateDto dto) {
        Organization organization = new Organization();
        organization.setOrganizationName(dto.getOrganizationName());
        return organization;
    }

    public OrganizationDto toDto(Organization save) {
        OrganizationDto organizationDto = new OrganizationDto();
        organizationDto.setId(save.getId());
        organizationDto.setOrganizationName(save.getOrganizationName());
        return organizationDto;
    }

    public void mapUpdate(Organization organization, OrganizationUpdateDto dto) {
        if (dto.getOrganizationName() != null && !dto.getOrganizationName().isBlank()) {
            organization.setOrganizationName(dto.getOrganizationName());
        }
    }

    public List<OrganizationDto> toDtoList(List<Organization> all) {
        return all
                .stream()
                .map(this::toDto)
                .toList();
    }
}
