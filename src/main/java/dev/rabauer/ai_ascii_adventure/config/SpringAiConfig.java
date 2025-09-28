package dev.rabauer.ai_ascii_adventure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class SpringAiConfig {

    @Bean
    public ChatModel defaultChatModel(AiProperties props) {
        return OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl(props.baseUrl()).build())
                .defaultOptions(OllamaOptions.builder()
                        .model(props.defaultModel())
                        .temperature(props.defaultTemperature())
                        .build())
                .build();
    }

    @Bean
    public ChatModel visionChatModel(AiProperties props) {
        return OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl(props.baseUrl()).build())
                .defaultOptions(OllamaOptions.builder()
                        .model(props.visionModel())
                        .temperature(props.visionTemperature())
                        .build())
                .build();
    }

    @Bean
    public ChatModel classificationChatModel(AiProperties props) {
        return OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl(props.baseUrl()).build())
                .defaultOptions(OllamaOptions.builder()
                        .model(props.classificationModel())
                        .temperature(props.classificationTemperature())
                        .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel defaultChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        return ChatClient.builder(defaultChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
