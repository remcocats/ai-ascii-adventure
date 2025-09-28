package dev.rabauer.ai_ascii_adventure.models;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;

public enum AiModel {
    OLLAMA_GPT_OSS(
            OllamaChatModel.builder()
                    .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
                    .defaultOptions(OllamaOptions.builder()
                            .model("gpt-oss:20b")
                            .temperature(0.3)
                            .build())
                    .build()
    ),
    OLLAMA_LLAVA(
            OllamaChatModel.builder()
                    .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
                    .defaultOptions(OllamaOptions.builder()
                            .model("llava")
                            .temperature(0.1)
                            .build())
                    .build()
    ),
    OLLAMA_LLAMA_32(
            OllamaChatModel.builder()
                    .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
                    .defaultOptions(OllamaOptions.builder()
                            .model("llama3.2")
                            .temperature(0.1)
                            .build())
                    .build()
    );

    public final ChatModel model;

    AiModel(ChatModel model) {
        this.model = model;
    }
}
