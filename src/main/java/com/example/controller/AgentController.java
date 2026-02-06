package com.example.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.dto.agent.ProjectAgentCreateDTO;
import com.example.model.dto.agent.ProjectAgentDTO;
import com.example.model.dto.databaseRole.DatabaseRoleCreateDTO;
import com.example.model.dto.databaseUser.ProjectDatabaseUserCreateDto;
import com.example.model.dto.special.UserDto;
import com.example.service.DatabaseUserService;
import com.example.service.ProjectAgentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@SkipActivityTracking
public class AgentController {

    private final DatabaseUserService databaseUserService;
    private final ProjectAgentService service;

    @PostMapping("/register")
    public ResponseEntity<ProjectAgentDTO> register(@RequestBody ProjectAgentCreateDTO dto) {
        ProjectAgentDTO projectAgentDTO = service.createWithDb(dto);
        return ResponseEntity.ok(projectAgentDTO);

    }

    @GetMapping("/updates")
    public ResponseEntity<List<UserDto>> getUpdatesForAgentWithRoles(@RequestParam String agentId, @RequestParam Long version) {
        List<UserDto> users = databaseUserService.getAll(agentId, version);
        return ResponseEntity.ok(users);
    }


    @PostMapping("/roles")
    public ResponseEntity<Void> registerRoles(@RequestParam("agentId")String agentId, @RequestBody List<DatabaseRoleCreateDTO> dto) {
        service.updateRoles(agentId,dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    public ResponseEntity<Void> registerUsers(@RequestParam("agentId")String agentId, @RequestBody List<ProjectDatabaseUserCreateDto> dto) {
        service.registerUsers(agentId,dto);
        return ResponseEntity.noContent().build();
    }
}
