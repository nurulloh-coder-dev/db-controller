package com.example.aop;

import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.example.config.CustomUserDetails;
import com.example.controller.active_users.UserSessionRedisService;

@Aspect
@Component
@AllArgsConstructor
public class UserActivityTracker {

    private final UserSessionRedisService userSessionService;

    @AfterReturning(pointcut = """
            (execution(* com.example.controller.*.*(..)))
             && !@within(com.example.aop.SkipActivityTracking)
              && !@annotation(com.example.aop.SkipActivityTracking)""")
    public void updateRedis() {
        if (userSessionService.isRedisRunning()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
                userSessionService.markActive(principal.getUserId());
            }
        }
    }
}
