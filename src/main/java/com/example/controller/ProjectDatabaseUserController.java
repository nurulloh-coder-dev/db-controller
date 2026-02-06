package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.model.dto.special.AuthUserDbsResponse;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserDto;
import com.example.model.dto.databaseUser.ProjectDatabaseUserUpdateDto;
import com.example.service.ProjectDatabaseUserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/databaseUser")
@AllArgsConstructor
public class ProjectDatabaseUserController {
    private final ProjectDatabaseUserService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDatabaseUserDto> create(@RequestBody ProjectDatabaseUserCreateDto createDto) {
        return new ResponseEntity<>(service.create(createDto), HttpStatus.CREATED);
    }

    @PostMapping(params = "databases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> create(@RequestBody ProjectDatabaseUserCreateDto createDto, @RequestParam("databases") List<String> databasesId) {
        service.create(createDto,databasesId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDatabaseUserDto>> getAll() {
        return new ResponseEntity<>(service.getAll(),HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDatabaseUserDto> get(@PathVariable String id) {
        return new ResponseEntity<>(service.get(id), HttpStatus.OK);
    }

    @GetMapping("/databases")
    public ResponseEntity<List<AuthUserDbsResponse>> getUserDb() {
        List<AuthUserDbsResponse> authUserDatabases = service.getAuthUserDatabases();
        return new ResponseEntity<>(authUserDatabases, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDatabaseUserDto> update(@RequestBody ProjectDatabaseUserUpdateDto updateDto, @PathVariable String id) {
        return new ResponseEntity<>(service.update(id,updateDto), HttpStatus.OK);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectDatabaseUserDto> updatePatch(@RequestBody ProjectDatabaseUserUpdateDto updateDto, @PathVariable String id) {
        return new ResponseEntity<>(service.updatePatch(id,updateDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }


    @PostMapping("/{dbUserId}/attach/{authId}")
    public ResponseEntity<Void> attachUser(@PathVariable String dbUserId, @PathVariable String authId){
        service.attachUser(dbUserId,authId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
