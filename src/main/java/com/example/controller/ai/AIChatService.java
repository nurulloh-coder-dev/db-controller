package com.example.controller.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import com.example.model.dto.special.ChatMessage;

@Service
public class AIChatService {

    private final ChatClient chatClient;

    public AIChatService(ChatClient.Builder builder,
                         ChatMemory chatMemory,
                         VectorStore vectorStore) {

        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .build();
    }

    public Flux<String> respond(ChatMessage chatMessage) {

        String systemPrompt = """ 
                You are an AI assistant for this web application.
               
                Use chat history ONLY to understand context and follow-up questions.
                Use application information as the single source of truth.
                If there is a conflict, application information is always correct.
                
                If information is missing, say you are unsure and explain briefly.
                
                """;

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(chatMessage.getMessage())
                .stream()
                .content();
    }
}
