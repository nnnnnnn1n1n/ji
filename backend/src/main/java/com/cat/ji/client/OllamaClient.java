
package com.cat.ji.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean thinking;

    private static final String SYSTEM_PROMPT = """
            คุณเป็นผู้ช่วยสนทนาเกี่ยวกับแมวชื่อจี้

            ตอบเป็นภาษาไทยอย่างเป็นธรรมชาติ อบอุ่น และตรงคำถาม
            ใช้ความทรงจำที่ได้รับเป็นแหล่งข้อมูลหลัก
            ห้ามแต่งรายละเอียดที่ไม่มีอยู่ในความทรงจำ
            หากไม่มีข้อมูล ให้บอกตรง ๆ ว่าไม่มีข้อมูลเพียงพอ

            แสดงเฉพาะคำตอบที่ผู้ใช้ต้องการ
            ห้ามบรรยายขั้นตอนการคิดหรือการวิเคราะห์ของตัวเอง
            ห้ามทวนคำสั่งหรืออธิบายว่ากำลังจะทำอะไร
            ตอบกระชับ เว้นแต่ผู้ใช้ขอรายละเอียดเพิ่มเติม
            """;

    public OllamaClient(
            @Value("${ji.memory.thinking:false}") boolean thinking,
            ObjectMapper objectMapper) {

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();

        this.objectMapper = objectMapper;
        this.thinking = thinking;
    }

    public String chat(String message) {

        long startTime = System.nanoTime();
        StringBuilder answer = new StringBuilder();

        System.out.println();
        System.out.println("========== OLLAMA ==========");
        System.out.println("[OLLAMA] Model: qwen3:1.7b");
        System.out.println("[OLLAMA] Thinking enabled: " + thinking);
        System.out.println("[OLLAMA] Starting...");
        System.out.println("============================");

        restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OllamaRequest(
                        "qwen3:4b",
                        message,
                        SYSTEM_PROMPT,
                        false,
                        thinking,
                        new OllamaOptions(null, null)
                ))
                .exchange((request, response) -> {

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "Ollama returned HTTP "
                                        + response.getStatusCode());
                    }

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    response.getBody(),
                                    StandardCharsets.UTF_8))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank()) {
                                continue;
                            }

                            JsonNode root = objectMapper.readTree(line);

                            JsonNode thinkingNode = root.get("thinking");
                            JsonNode responseNode = root.get("response");

                            if (thinkingNode != null
                                    && thinkingNode.isTextual()
                                    && !thinkingNode.asText().isEmpty()) {
                                System.out.print(thinkingNode.asText());
                                System.out.flush();
                            }

                            if (responseNode != null
                                    && responseNode.isTextual()) {
                                String chunk = responseNode.asText();
                                answer.append(chunk);
                            }

                            JsonNode errorNode = root.get("error");
                            if (errorNode != null) {
                                throw new IllegalStateException(
                                        "Ollama error: "
                                                + errorNode.asText());
                            }

                            if (root.path("done").asBoolean(false)) {
                                System.out.println();
                                System.out.println(
                                        "[OLLAMA] Prompt tokens: "
                                                + root.path("prompt_eval_count")
                                                        .asInt(0));
                                System.out.println(
                                        "[OLLAMA] Generated tokens: "
                                                + root.path("eval_count")
                                                        .asInt(0));
                                System.out.println(
                                        "[OLLAMA] Generation seconds: "
                                                + root.path("eval_duration")
                                                        .asDouble(0)
                                                / 1_000_000_000);
                            }
                        }
                    }

                    return null;
                });

        double totalSeconds =
                (System.nanoTime() - startTime) / 1_000_000_000.0;

        System.out.println();
        System.out.println("========== OLLAMA ANSWER ==========");
        System.out.println(answer);
        System.out.println("[OLLAMA] Total seconds: " + totalSeconds);
        System.out.println("===================================");

        return answer.toString();
    }

    private record OllamaRequest(
            String model,
            String prompt,
            String system,
            boolean stream,
            boolean think,
            OllamaOptions options) {
    }

    private record OllamaOptions(
            Integer num_predict,
            Integer num_ctx) {
    }
}
