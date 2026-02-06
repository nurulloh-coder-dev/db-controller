package com.example.controller.active_users;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserSessionService {


    private final UserSessionRepository repository;

    public ActiveUsersResponse getActiveUsers(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(dateStr,formatter);
        Instant startOfDay = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        List<ActiveUserProjection> projections = repository.getActiveUsersByGivenDate(startOfDay);


        List<ActiveUserDTO> activeUsersDB = projections.stream()
                .map(p -> new ActiveUserDTO(
                        p.getUserId(),
                        p.getUserName(),
                        p.getEmail(),
                        p.getOrganizationId(),
                        p.getOrganizationName(),
                        p.getSessionStart(),
                        p.getLastActivity(),
                        null,
                        null
                ))
                .toList();

        activeUsersDB.forEach(user -> {
            Instant sessionStart = user.getSessionStart();
            Instant lastActivity = user.getLastActivity();
            user.setTotalTimeSpent(Duration.between(sessionStart, lastActivity).toSeconds());
            user.setStatus(user.getLastActivity().isAfter(Instant.now().minus(Duration.ofMinutes(1)))
                    ? "ACTIVE"
                    : "INACTIVE");
        });
        ArrayList<ActiveUserDTO> activeUsers = new ArrayList<>(activeUsersDB
                .stream()
                .collect(Collectors.toMap(
                        ActiveUserDTO::getUserId,
                        Function.identity(),
                        (acc1, acc2) -> {
                            acc1.setTotalTimeSpent(acc1.getTotalTimeSpent() + acc2.getTotalTimeSpent());
                            return acc1;
                        }
                )).values());

        ActiveUsersResponse activeUsersResponse = new ActiveUsersResponse();
        activeUsersResponse.setActiveUsers(activeUsers);
        activeUsersResponse.setTotalSessionsToday(activeUsers.size());
        activeUsersResponse.setCurrentActiveCount(repository.countAllCurrentlyActives(Instant.now().minus(Duration.ofMinutes(1))));
        long totalDuration = activeUsersDB.stream()
                .mapToLong(ActiveUserDTO::getTotalTimeSpent)
                .sum();
        long avgDuration = activeUsersDB.isEmpty() ? 0 : totalDuration / activeUsersDB.size();
        activeUsersResponse.setAvgSessionDuration(avgDuration);
        return activeUsersResponse;
    }


}
