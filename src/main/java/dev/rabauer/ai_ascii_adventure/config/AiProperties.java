package dev.rabauer.ai_ascii_adventure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        @DefaultValue("http://localhost:11434") String baseUrl,
        @DefaultValue("gpt-oss:20b") String defaultModel,
        @DefaultValue("llava") String visionModel,
        @DefaultValue("llama3.2") String classificationModel,
        @DefaultValue("0.3") double defaultTemperature,
        @DefaultValue("0.1") double visionTemperature,
        @DefaultValue("0.1") double classificationTemperature,
        @DefaultValue("30") int npcDecisionDelaySeconds
) {
}
