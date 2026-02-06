package com.example.model.dto.jar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JarVersionDto {
    private String id;
    private String version;
    private Long sizeInBytes;
    private String description;
    private Instant releaseDate;

    public JarVersionDto(String version, Long sizeInBytes, Instant releaseDate) {
        this.version = version;
        this.sizeInBytes = sizeInBytes;
        this.releaseDate = releaseDate;
    }
}