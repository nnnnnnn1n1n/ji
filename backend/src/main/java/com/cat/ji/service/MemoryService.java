
package com.cat.ji.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class MemoryService {

    private final Path memoryFile;

    public MemoryService(
            @Value("${ji.memory.file}") String memoryFile
    ) {
        this.memoryFile = Path.of(memoryFile);
    }

    public String getMemory() {
        try {
            if (!Files.exists(memoryFile)) {
                throw new IllegalStateException(
                        "Memory file not found: " + memoryFile
                );
            }

            return Files.readString(
                    memoryFile,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read memory file: " + memoryFile,
                    e
            );
        }
    }
}
