package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.dto.organization.OrganizationCreateDto;
import com.example.model.dto.organization.OrganizationDto;
import com.example.model.dto.organization.OrganizationUpdateDto;
import com.example.service.OrganizationService;
import com.example.validator.AuthUserValidator;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final AuthUserValidator authUserValidator;

    @PostMapping
    @SkipActivityTracking
    public ResponseEntity<OrganizationDto> create(@RequestBody OrganizationCreateDto createDto, @RequestParam(name = "lang",required = false,defaultValue = "en") String lang) {
        OrganizationDto organizationDto = organizationService.create(createDto,lang);
        return new ResponseEntity<>(organizationDto, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrganizationDto>> getAll() {
        List<OrganizationDto> all = organizationService.getAll();
        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<OrganizationDto> get() {
        String organizationId = authUserValidator.validateAuthenticationAndGetOrganizationId();
        OrganizationDto organizationDto = organizationService.get(organizationId);
        return new ResponseEntity<>(organizationDto, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<OrganizationDto> update(@RequestBody OrganizationUpdateDto updateDto) {
        String organizationId = authUserValidator.validateAuthenticationAndGetOrganizationId();
        OrganizationDto organizationDto = organizationService.update(organizationId, updateDto);
        return new ResponseEntity<>(organizationDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
