package com.example.controller.ai;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.example.model.dto.special.ChatMessage;

@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class AIChatController {

    private final AIChatService aIChatService;

    @PostMapping
    public Flux<String> respond(@RequestBody ChatMessage chatMessage){
        return aIChatService.respond(chatMessage);
    }
}
