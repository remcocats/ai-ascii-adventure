package dev.rabauer.ai_ascii_adventure;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;


@Service
public class AiService {

    private final ChatModel chatModel;

    @Autowired
    public AiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public ChatClient createChatClient(boolean withMemory) {
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        if (withMemory) {
            ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
            builder = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());
        }
        return builder.build();
    }

    public void generateNewStoryPart(ChatClient chatClient, String textPrompt, Object tools, OnNextFunction onNext, OnCompleteFunction onComplete) {
        chatClient
                .prompt(new Prompt(textPrompt))
                .tools(tools)
                .stream()
                .chatClientResponse()
                .doOnNext(onNext)
                .doOnComplete(onComplete)
                .subscribe();
    }

    public void generateAsciiArt(ChatClient chatClient, String textPrompt, OnCompleteWithResultFunction onComplete) {
        StringBuilder receivedText = new StringBuilder();
        chatClient
                .prompt(new Prompt(textPrompt))
                .stream()
                .chatClientResponse()
                .doOnNext(response -> receivedText.append(response.chatResponse().getResult().getOutput().getText()))
                .doOnComplete(() -> onComplete.accept(receivedText.toString()))
                .subscribe();
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
}
