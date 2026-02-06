package com.example.controller.active_users;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor   // Important for potential future use
@AllArgsConstructor
public class ActiveUserDTO {
    private String userId;
    private String userName;
    private String email;
    private String organizationId;
    private String organizationName;
    private Instant sessionStart;
    private Instant lastActivity;
    private Long totalTimeSpent;
    private String status;
}