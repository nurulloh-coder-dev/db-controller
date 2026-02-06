package com.example.controller.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, String> {
    Optional<DailyReport> findByDate(LocalDate date);
}
