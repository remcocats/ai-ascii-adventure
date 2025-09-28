package dev.rabauer.ai_ascii_adventure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * Factory that resolves ChatClient instances for different logical AI model roles.
 * Decouples callers from concrete provider/model wiring.
 */
@Service
public class AiClientFactory {

    private final ChatClient defaultClient;
    private final ChatModel visionChatModel;
    private final ChatModel classificationChatModel;

    public AiClientFactory(ChatClient defaultClient,
                           ChatModel visionChatModel,
                           ChatModel classificationChatModel) {
        this.defaultClient = defaultClient;
        this.visionChatModel = visionChatModel;
        this.classificationChatModel = classificationChatModel;
    }

    public ChatClient getClient(AiModel model) {
        return getClient(model, false);
    }

    public ChatClient getClient(AiModel model, boolean withMemory) {
        ChatClient.Builder builder;
        switch (model) {
            case VISION -> builder = ChatClient.builder(visionChatModel);
            case CLASSIFICATION -> builder = ChatClient.builder(classificationChatModel);
            case DEFAULT -> {
                // Default ChatClient is provided by Spring AI autoconfiguration or custom @Bean
                return withMemory ? withMemory(defaultClient) : defaultClient;
            }
            default -> throw new IllegalArgumentException("Unsupported model: " + model);
        }
        ChatClient client = builder.build();
        return withMemory ? withMemory(client) : client;
    }

    private ChatClient withMemory(ChatClient base) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        return base.mutate().defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }
}
