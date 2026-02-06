package com.example.controller.ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.example.service.FileService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final DailyReportRepository dailyReportRepository;

    public AIService(ChatClient.Builder chatClient, FileService fileService, ObjectMapper objectMapper, DailyReportRepository dailyReportRepository) {
        this.chatClient = chatClient.build();
        this.fileService = fileService;
        this.objectMapper = objectMapper;
        this.dailyReportRepository = dailyReportRepository;
    }

    public DailyReport getAppInfo() {
        return getNewInfo(LocalDate.now());
    }
    public DailyReport getAppInfo(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = LocalDate.parse(dateStr,formatter);
        Optional<DailyReport> byDate = dailyReportRepository.findByDate(date);
        return byDate.orElseGet(() -> getNewInfo(date));
    }

    @SneakyThrows
    private DailyReport getNewInfo(LocalDate date) {
        String logs = fileService.readFile(date);
        if (logs.isEmpty()) {
            return new DailyReport();
        }
        String json = getJson(logs);
        DailyReport dailyReport = objectMapper.readValue(json, DailyReport.class);
        dailyReport.setDate(LocalDate.now());
        return dailyReportRepository.save(dailyReport);
    }


    public String getJson(String text) {
        String systemPrompt = """
                You are a strict log-analysis engine.
                
                Your task is to analyze raw application logs and return EXACT statistics in JSON.
                Accuracy is more important than verbosity.
                
                ====================
                INPUT GUARANTEES
                ====================
                - Each log line represents exactly ONE HTTP request
                - Each line contains:
                  - Status prefix: "Success:" or "Error:"
                  - HTTP method (GET, POST, PUT, DELETE)
                  - Endpoint path
                  - Execution time in milliseconds (ONLY for Success lines)
                  - A timestamp (ISO or readable format) — you MAY use it for peakHours
                
                ====================
                NORMALIZATION RULES (DO FIRST)
                ====================
                1. Normalize endpoint paths BEFORE counting:
                   - If a path segment after the API base looks like an ID
                     (UUID, long hex string, numeric ID),
                     replace it with "/{id}"
                   - Example:
                     /api/v1/user/270ee809-dc23-490a-82e0-9630f5e1ec80
                     → /api/v1/user/{id}
                
                2. Group requests STRICTLY by:
                   - normalized path
                   - HTTP method
                   (Never mix methods for the same path)
                
                ====================
                COUNTING RULES
                ====================
                For each (path + method):
                
                - totalReqPerDay:
                  Count ALL requests (Success + Error)
                
                - successfulRequests:
                  Lines starting with "Success:"
                
                - failedRequests:
                  Lines starting with "Error:"
                
                ====================
                CALCULATION RULES
                ====================
                - avgResponseTime:
                  Average ONLY execution times from Success lines
                  Round to nearest integer
                
                - successRate:
                  (successfulRequests / totalReqPerDay) * 100
                  Round to 1 decimal place
                
                - failureRate:
                  (failedRequests / totalReqPerDay) * 100
                  Round to 1 decimal place
                
                - successRate + failureRate MUST equal exactly 100.0
                
                ====================
                PEAK HOURS RULE
                ====================
                - Extract hour from timestamp
                - Format hour as "HH:00" (24-hour format)
                - Count requests per hour
                - Return up to TOP 3 hours by request count
                - If fewer than 3 exist, return only those
                
                ====================
                OUTPUT FORMAT
                ====================
                Return ONLY valid JSON.
                No markdown. No comments. No explanation.
                
                {
                  "endpoints": [
                    {
                      "path": "/api/v1/users/{id}",
                      "method": "GET",
                      "totalReqPerDay": 10,
                      "avgResponseTime": 120,
                      "successRate": 90.0,
                      "failureRate": 10.0,
                      "peakHours": ["09:00", "14:00"]
                    }
                  ]
                }
                
                ====================
                FINAL VALIDATION (MANDATORY)
                ====================
                Before returning:
                - Ensure totals match counted lines
                - Ensure percentages are mathematically correct
                - Ensure JSON is syntactically valid
                """;


        return chatClient.prompt()
                .user(text)
                .system(systemPrompt)
                .call()
                .content();
    }
}
