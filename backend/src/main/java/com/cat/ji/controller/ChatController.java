package com.cat.ji.controller;

import com.cat.ji.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request.message());
    }

    public record ChatRequest(String message) {
    }
}