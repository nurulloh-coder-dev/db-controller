package com.example;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.example.model.entity.AuthRole;
import com.example.repository.AuthRoleRepository;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableAspectJAutoProxy
@EnableMethodSecurity
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //
    @Bean
    public CommandLineRunner runner(
            AuthRoleRepository authRoleRepository) {
        return args -> {
            Optional<AuthRole> admin = authRoleRepository.getAdmin();
            if (admin.isEmpty()) {
                authRoleRepository.save(new AuthRole("ADMIN", "ADMIN"));
            }
            Optional<AuthRole> developer = authRoleRepository.getDeveloper();
            if (developer.isEmpty()) {
                authRoleRepository.save(new AuthRole("DEVELOPER", "DEVELOPER"));
            }
        };
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(6)
                .build();
    }


}
