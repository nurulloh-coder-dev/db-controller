package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.model.dto.jar.AdminJarVersionDto;
import com.example.model.dto.jar.JarVersionDto;
import com.example.model.dto.jar.JarVersionCreateDto;
import com.example.model.dto.jar.JarVersionUpdateDto;
import com.example.service.JarVersionService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jar")
@AllArgsConstructor
public class JarVersionController {

    private final JarVersionService jarVersionService;

    @GetMapping
    public ResponseEntity<List<JarVersionDto>> getAll() {
        List<JarVersionDto> all = jarVersionService.getAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminJarVersionDto> getAllForAdmin() {
        AdminJarVersionDto all = jarVersionService.getAllForAdmin();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JarVersionDto> get(@PathVariable("id") String id) {
        JarVersionDto jarVersionDto = jarVersionService.get(id);
        return ResponseEntity.ok(jarVersionDto);
    }

    @PostMapping
    public ResponseEntity<JarVersionDto> create(@RequestBody JarVersionCreateDto jarVersionCreateDto) {
        JarVersionDto jarVersionDto = jarVersionService.create(jarVersionCreateDto);
        return new ResponseEntity<>(jarVersionDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JarVersionDto> update(@PathVariable String id, @RequestBody JarVersionUpdateDto jarVersionUpdateDto) {
        JarVersionDto jarVersionDto = jarVersionService.update(id,jarVersionUpdateDto);
        return new ResponseEntity<>(jarVersionDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id ) {
        jarVersionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
