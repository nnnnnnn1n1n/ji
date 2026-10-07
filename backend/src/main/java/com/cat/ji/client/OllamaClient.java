package com.cat.ji.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class OllamaClient {

    private final RestClient restClient;

    public OllamaClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    public String chat(String message) {

        System.out.println();
        System.out.println("========== OLLAMA ==========");
        System.out.println("[OLLAMA] Message: " + message);
        System.out.println("[OLLAMA] Model: qwen3:4b");
        System.out.println("[OLLAMA] Starting...");
        System.out.println("============================");

        StringBuilder answer = new StringBuilder();

        restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OllamaRequest(
                        "qwen3:4b",
                        message,
                        true,
                        true
                ))
                .exchange((request, response) -> {

                    System.out.println();
                    System.out.println("========== OLLAMA THINKING ==========");

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    response.getBody(),
                                    StandardCharsets.UTF_8))) {

                        String line;

                        while ((line = reader.readLine()) != null) {

                            if (line.isBlank()) {
                                continue;
                            }

                            String thinking = extractJsonValue(line, "thinking");
                            String responseText = extractJsonValue(line, "response");

                            if (thinking != null && !thinking.isEmpty()) {
                                System.out.print(thinking);
                                System.out.flush();
                            }

                            if (responseText != null && !responseText.isEmpty()) {
                                answer.append(responseText);
                            }
                        }
                    }

                    return null;
                });

        System.out.println();
        System.out.println("======================================");

        System.out.println();
        System.out.println("========== OLLAMA ANSWER =============");
        System.out.println(answer);
        System.out.println("======================================");

        return answer.toString();
    }

    private String extractJsonValue(String json, String key) {

        String search = "\"" + key + "\":\"";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start += search.length();

        int end = json.indexOf("\"", start);

        if (end == -1) {
            return null;
        }

        return json.substring(start, end);
    }

    private record OllamaRequest(
            String model,
            String prompt,
            boolean stream,
            boolean think
    ) {
    }
}