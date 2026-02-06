package com.example.controller.active_users;

import lombok.Data;

import java.util.List;

@Data
public class ActiveUsersResponse {
    private List<ActiveUserDTO> activeUsers;
    private Integer currentActiveCount;
    private Integer totalSessionsToday;
    private Long avgSessionDuration;
}