package com.example.model.dto.special;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserDto {
    private String username;
    private String password;
    private List<String> roles;
    private Long version;
    private boolean deleted;
    private String dbName;
}
