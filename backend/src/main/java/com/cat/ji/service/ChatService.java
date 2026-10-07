package com.cat.ji.service;

import com.cat.ji.client.OllamaClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OllamaClient ollamaClient;

    public ChatService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String chat(String message) {
        return ollamaClient.chat(message);
    }
}