package dev.rabauer.ai_ascii_adventure;

import dev.rabauer.ai_ascii_adventure.ai.RouteClassification;
import dev.rabauer.ai_ascii_adventure.models.AiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class AiService {

    private final java.util.Set<reactor.core.Disposable> activeDisposables = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private final Map<String, String> routes = Map.of(
            "npc", """
    You are a npc a friend of the hero, you can talk to him and to other npcs.
    You can also add items to your inventory or weapons to your hero.
    You can exchange items and weapons with other npcs and the hero.
    You can use spells to heal or cast a spell.
    You can choose what to do next in the quest, but you always with the group where you started in.
    """,
            "goblin", """
                    You are a goblin fighting against the hero and the other npcs.
                    You have health based on the information of the dnd 5th edition.
                    """,
            "hero", """
                    You are the hero, you can talk to the npcs and the other npcs.
                    """
    );

    public AiService() {
    }

    public ChatClient createChatClient(boolean withMemory, ChatModel chatModel) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        if (withMemory) {
            ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
            builder = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());
        }
        return builder.build();
    }

    public reactor.core.Disposable generateNewStoryPart(ChatClient chatClient, String character, String textPrompt, OnNextFunction onNext, OnCompleteFunction onComplete, Object... tools) {
        assert character != null;

        String selectedRoute = classifyInquiry(character, routes.keySet());

        String specializedPrompt = routes.get(selectedRoute);

        reactor.core.Disposable d = chatClient
                .prompt(new Prompt(textPrompt))
                .user(specializedPrompt + "\n\nCharacter that is chosen: " + character + "\n\n")
                .tools(tools)
                .stream()
                .chatClientResponse()
                .doOnNext(onNext)
                .doOnComplete(onComplete)
                .subscribe();
        activeDisposables.add(d);
        return d;
    }

    /**
     * Analyzes the customer inquiry and determines the most appropriate support route.
     * Uses LLM to understand the context and classify the inquiry type.
     */
    private String classifyInquiry(String inquiry, Iterable<String> availableRoutes) {
        String classificationPrompt = constructPrompt(inquiry, availableRoutes);

        RouteClassification classification = this.createChatClient(false, AiModel.OLLAMA_LLAMA_32.model)
                .prompt()
                .user(classificationPrompt)
                .call()
                .entity(RouteClassification.class);

        assert classification != null;

        return classification.selectedRoute();
    }

    /**
     * Constructs a classification prompt to help the LLM decide which character should choose
     */
    private String constructPrompt(String character, Iterable<String> availableRoutes) {
        return String.format("""
                You are a dungeon master. Analyze the character that is on his turn. 
                which kind of character is it of these options: %s
                
                Consider:
                - The role
                - If he is the hero
                - If he is a friend of the hero
                - If he is a enemy
                
                The character that is chosen is: %s
                
                Respond in JSON format like this start with '{' and end with '}':
                {
                    "reasoning": "Brief explanation of why this role is chosen",
                    "selectedRoute": "The exact role of the character in lowercase",
                }
                """, availableRoutes, character);
    }

    public reactor.core.Disposable generateAsciiArt(ChatClient chatClient, String textPrompt, OnCompleteWithResultFunction onComplete) {
        StringBuilder receivedText = new StringBuilder();
        reactor.core.Disposable d = chatClient
                .prompt(new Prompt(textPrompt))
                .stream()
                .chatClientResponse()
                .doOnNext(response -> {
                            assert response.chatResponse() != null;
                            receivedText.append(response.chatResponse()
                                    .getResult()
                                    .getOutput()
                                    .getText());
                        }
                )
                .doOnComplete(() -> onComplete.accept(receivedText.toString()))
                .subscribe();
        activeDisposables.add(d);
        return d;
    }


    @FunctionalInterface
    public interface OnNextFunction extends Consumer<ChatClientResponse> {
    }

    @FunctionalInterface
    public interface OnCompleteFunction extends Runnable {
    }

    @FunctionalInterface
    public interface OnCompleteWithResultFunction extends Consumer<String> {
    }
    public void cancelAllActive() {
        for (reactor.core.Disposable d : activeDisposables) {
            try {
                if (d != null && !d.isDisposed()) d.dispose();
            } catch (Exception e) {
                log.warn("Error disposing active AI request", e);
            }
        }
        activeDisposables.clear();
    }

    @jakarta.annotation.PreDestroy
    public void onShutdown() {
        log.info("Graceful shutdown: cancelling {} in-flight AI requests", activeDisposables.size());
        cancelAllActive();
    }
}
