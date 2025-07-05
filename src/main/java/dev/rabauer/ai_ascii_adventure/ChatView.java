package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
public class ChatView extends HorizontalLayout {

    @Autowired
    public ChatView(ChatModel chatModel) {
        TextField txtChat = new TextField();
        ChatResponse response = chatModel.call(
                new Prompt(
                        "Generate the names of 5 famous pirates."
                ));
        txtChat.setValue(response.getResult().getOutput().getText());
        this.add(txtChat);
    }
}
