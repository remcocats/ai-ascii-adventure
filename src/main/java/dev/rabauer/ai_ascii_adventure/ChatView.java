package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
public class ChatView extends HorizontalLayout {

    @Autowired
    public ChatView(ChatModel chatModel) {
        TextArea txtResponse = new TextArea();
        txtResponse.setEnabled(false);
        this.add(txtResponse);


        TextField txtChat = new TextField();
        Button btnSendPrompt = new Button("Send prompt");
        btnSendPrompt.addClickListener(_ -> callPrompt(chatModel, new Prompt(txtChat.getValue()), txtResponse));

        this.add(txtChat, btnSendPrompt);
    }

    private static void callPrompt(ChatModel chatModel, Prompt prompt, TextArea txtChat) {
        UI current = UI.getCurrent();
        chatModel
                .stream(prompt)
                .subscribe(
                        response ->
                                current.access(
                                        () ->
                                            txtChat.setValue(txtChat.getValue() + response.getResult().getOutput().getText())
                                )
                );
    }
}
