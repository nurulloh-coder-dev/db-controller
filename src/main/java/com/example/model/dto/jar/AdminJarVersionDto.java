package com.example.model.dto.jar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminJarVersionDto {
    List<JarVersionDto> newVersions;
    List<JarVersionDto> approvedVersions;
}
