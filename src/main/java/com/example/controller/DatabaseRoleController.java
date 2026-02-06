package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.dto.databaseRole.DatabaseRoleDTO;
import com.example.service.DatabaseRoleService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/database-role")
@AllArgsConstructor
public class DatabaseRoleController {

    private final DatabaseRoleService service;

    @GetMapping("/all/{id}")
    public ResponseEntity<List<DatabaseRoleDTO>> getAll(@PathVariable String id) {
        List<DatabaseRoleDTO> allByDBId = service.getAllByDBId(id);
        return ResponseEntity.ok(allByDBId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatabaseRoleDTO> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<DatabaseRoleDTO> create(@RequestBody DatabaseRoleCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatabaseRoleDTO> update(@PathVariable("id") String id, @RequestBody DatabaseRoleDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok("Role has deleted successfully!");
    }

}
