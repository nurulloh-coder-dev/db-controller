package com.example.controller.ai;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/info")
@AllArgsConstructor
public class AIController {

    private final AIService aIService;

//    @GetMapping("/instant")
//    public ResponseEntity<DailyReport> getReport(){
//        DailyReport appInfo = aIService.getAppInfo();
//        return ResponseEntity.ok(appInfo);
//    }
//    @GetMapping("/{date}")
//    public ResponseEntity<DailyReport> getReportWithDate(@PathVariable String date){
//        DailyReport appInfo = aIService.getAppInfo(date);
//        return ResponseEntity.ok(appInfo);
//    }
}
