package com.cat.ji.service;

import com.cat.ji.client.OllamaClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OllamaClient ollamaClient;
    private final MemoryService memoryService;

    public ChatService(
            OllamaClient ollamaClient,
            MemoryService memoryService) {
        this.ollamaClient = ollamaClient;
        this.memoryService = memoryService;
    }

    public String chat(String message) {

        String memory = memoryService.getMemory();

        String prompt = """
                คุณกำลังสนทนาเกี่ยวกับแมวชื่อจี้

                ข้อมูลความทรงจำเกี่ยวกับจี้:
                ---
                %s
                ---

                ข้อความจากผู้ใช้:
                %s

                คำแนะนำ:
                - ตอบเป็นภาษาไทย เว้นแต่ผู้ใช้จะใช้ภาษาอื่น
                - ใช้ข้อมูลความทรงจำข้างต้นเป็นแหล่งข้อมูลหลัก
                - รักษารายละเอียดและเรื่องราวตามต้นฉบับ
                - หากไม่มีข้อมูลที่ตอบคำถามได้ อย่าแต่งเรื่องขึ้นมาเอง
                - ตอบอย่างเป็นธรรมชาติและอบอุ่น
                """.formatted(memory, message);

        return ollamaClient.chat(prompt);
    }
}
