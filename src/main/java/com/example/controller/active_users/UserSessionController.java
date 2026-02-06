package com.example.controller.active_users;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/admin/active-users")
public class UserSessionController {


    private final UserSessionService userSessionService;

    @GetMapping("/{date}")
    public ResponseEntity<ActiveUsersResponse> getAllActiveUsersByDate(
            @PathVariable("date")String date) {
        ActiveUsersResponse activeUsers = userSessionService.getActiveUsers(date);
        return ResponseEntity.ok(activeUsers);
    }
}
