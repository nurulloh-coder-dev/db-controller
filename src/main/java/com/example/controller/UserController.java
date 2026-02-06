package com.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.dto.authUser.AuthUserCreateDto;
import com.example.model.dto.authUser.AuthUserDto;
import com.example.model.dto.authUser.AuthUserUpdateDto;
import com.example.model.dto.special.AuthUserCreateWithOrganization;
import com.example.model.entity.WebSettings;
import com.example.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Foydalanuvchilar bilan ishlash uchun apilar")
public class UserController {

    private final UserService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthUserDto> create(@RequestBody AuthUserCreateDto dto) {
        AuthUserDto authUserDto = service.create(dto);
        return new ResponseEntity<>(authUserDto, HttpStatus.CREATED);
    }


    @PostMapping("/organization")
    @SkipActivityTracking
    public ResponseEntity<AuthUserDto> createWithOrganization(
            @RequestBody AuthUserCreateWithOrganization dto,
            @RequestParam(name = "lang", required = false, defaultValue = "en") String lang) {
        AuthUserDto authUserDto = service.createWithOrganization(dto, lang);
        return new ResponseEntity<>(authUserDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthUserDto> update(@RequestBody AuthUserUpdateDto dto, @PathVariable String id) {
        AuthUserDto authUserDto = service.update(id, dto);
        return new ResponseEntity<>(authUserDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AuthUserDto> updateIgnoreNull(@RequestBody AuthUserUpdateDto dto, @PathVariable String id) {
        AuthUserDto authUserDto = service.updateIgnoreNull(id, dto);
        return new ResponseEntity<>(authUserDto, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthUserDto> get(@PathVariable String id) {
        AuthUserDto authUserDto = service.get(id);
        return new ResponseEntity<>(authUserDto, HttpStatus.OK);
    }
    @GetMapping("/profile")
    public ResponseEntity<AuthUserDto> getProfile() {
        AuthUserDto authUserDto = service.getProfile();
        return new ResponseEntity<>(authUserDto, HttpStatus.OK);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuthUserDto>> getAll() {
        List<AuthUserDto> all = service.getAll();
        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuthUserDto>> getAllBySearch(
            @RequestParam(required = false) String search) {
        List<AuthUserDto> users = service.getAllBySearch(search);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/settings")
    public ResponseEntity<Void> changeSettings(@RequestBody WebSettings settings) {
        service.changeSettings(settings);
        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/password")
    public ResponseEntity<Boolean> checkPassword(@PathParam("password") String password){
        Boolean resp = service.handlePasswordCheck(password);
        return ResponseEntity.ok(resp);
    }
    @PostMapping("/password")
    public ResponseEntity<Void> savePassword(@PathParam("password") String password){
        service.handlePasswordUpdate(password);
        return ResponseEntity.noContent().build();
    }
}
