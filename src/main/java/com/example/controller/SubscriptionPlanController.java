package com.example.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.model.dto.sub_plans.SubscriptionPlanCreateDto;
import com.example.model.dto.sub_plans.SubscriptionPlanDto;
import com.example.model.dto.sub_plans.SubscriptionPlanUpdateDto;
import com.example.service.SubscriptionPlanService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/subscription")
public class SubscriptionPlanController {
    private final SubscriptionPlanService service;

    @PostMapping
    public ResponseEntity<SubscriptionPlanDto> create(@RequestBody SubscriptionPlanCreateDto createDto) {
        SubscriptionPlanDto subscriptionPlanDto = service.create(createDto);
        return new ResponseEntity<>(subscriptionPlanDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanDto> get(@PathVariable String id) {
        SubscriptionPlanDto subscriptionPlanDto = service.get(id);
        return new ResponseEntity<>(subscriptionPlanDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanDto>> getAll() {
        List<SubscriptionPlanDto> all = service.getAll();
        return ResponseEntity.ok(all);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanDto> update(@PathVariable String id, @RequestBody SubscriptionPlanUpdateDto dto) {
        SubscriptionPlanDto update = service.update(id, dto);
        return ResponseEntity.ok(update);
    }
}
