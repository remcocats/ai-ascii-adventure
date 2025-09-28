package dev.rabauer.ai_ascii_adventure;

import dev.rabauer.ai_ascii_adventure.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AiPropertiesIntegrationTest {

    @Autowired
    private AiProperties aiProperties;

    @Test
    void properties_are_bound_from_application_properties() {
        assertThat(aiProperties).isNotNull();
        assertThat(aiProperties.baseUrl()).isEqualTo("http://localhost:11434");
        assertThat(aiProperties.defaultModel()).isEqualTo("gpt-oss:20b");
        assertThat(aiProperties.visionModel()).isEqualTo("llava");
        assertThat(aiProperties.classificationModel()).isEqualTo("llama3.2");
        assertThat(aiProperties.defaultTemperature()).isEqualTo(0.3);
        assertThat(aiProperties.visionTemperature()).isEqualTo(0.1);
        assertThat(aiProperties.classificationTemperature()).isEqualTo(0.1);
        assertThat(aiProperties.npcDecisionDelaySeconds()).isEqualTo(30);
    }
}
