package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.dto.agent.ProjectAgentCreateDTO;
import com.example.model.dto.agent.ProjectAgentDTO;
import com.example.model.dto.agent.ProjectAgentUpdateDTO;
import com.example.service.ProjectAgentService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/project-agent")
@SkipActivityTracking
public class ProjectAgentController {
    private final ProjectAgentService service;

    @GetMapping
    public ResponseEntity<List<ProjectAgentDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectAgentDTO> get(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ProjectAgentDTO> create(@RequestBody ProjectAgentCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectAgentDTO> update(@PathVariable("id") String id, @RequestBody ProjectAgentUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        service.delete(id);
        return ResponseEntity.ok("Agent has deleted successfully!");
    }

    @GetMapping("/getByDBUrl")
    public ResponseEntity<ProjectAgentDTO> getByDBUrl(@RequestParam String dbUrl) {
        return ResponseEntity.ok(service.getAgentByDBUrl(dbUrl));
    }
}
