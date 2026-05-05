package com.example.model.dto.special;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseUserRegistry {
    private String username;
    private List<String> roles = Collections.emptyList();
}

