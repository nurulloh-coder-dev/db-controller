package com.example.controller;


import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.aop.SkipActivityTracking;
import com.example.model.entity.JarHistory;
import com.example.service.JarDownloadService;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/download-app")
@SkipActivityTracking
public class JarDownloadController {

    private final JarDownloadService service;

    @GetMapping("/{name}")
    public ResponseEntity<Resource> downloadApp(@PathVariable("name") String name) {
        Optional<Resource> download = service.download(name);
        return download.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<JarHistory>> getHistory() {
        List<JarHistory> versions = service.getHistory();
        return ResponseEntity.ok(versions);
    }


}
