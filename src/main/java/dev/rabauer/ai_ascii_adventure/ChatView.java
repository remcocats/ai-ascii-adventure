package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.springframework.security.core.parameters.P;

@Route(value = "", layout = MainLayout.class)
public class ChatView extends SplitLayout {

    private static final String INITIAL_PROMPT =
            """
              The hero's name is "%s".     
            """;
    private final TextArea txtStory = new TextArea();
    private final TextArea txtAsciiArt = new TextArea();

    private final ChatModel chatModel;
    private String heroName;

    @Autowired
    public ChatView(ChatModel chatModel) {
        this.chatModel = chatModel;

        this.setSizeFull();
        this.addToPrimary(createAsciiArt());
        this.addToSecondary(createStoryComponent());

        openStartDialog();
    }

    private void openStartDialog()
    {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        dialog.setHeaderTitle("Input Hero name");

        TextField txtHeroName = new TextField("Hero name");
        txtHeroName.setValue("Hans");
        dialog.add(txtHeroName);

        Button saveButton = new Button("Ok");
        saveButton.addClickListener(buttonClickEvent ->
        {
            dialog.close();
            this.heroName = txtHeroName.getValue();

            callPrompt(new Prompt(
                    INITIAL_PROMPT.formatted(this.heroName)
            ));
        });
        dialog.getFooter().add(saveButton);
        dialog.open();
    }

    private Component createAsciiArt() {
        txtAsciiArt.setSizeFull();
        txtAsciiArt.setTitle("Graphics");

        ProgressBar prbHealth = new ProgressBar(0, 1, 1);
        prbHealth.setWidthFull();
        ProgressBar prbMana = new ProgressBar(0, 1, 1);
        prbMana.setWidthFull();

        HorizontalLayout hlStatusBar = new HorizontalLayout(prbHealth, prbMana);
        hlStatusBar.setWidthFull();
        return new VerticalLayout(txtAsciiArt, hlStatusBar);
    }

    private Component createStoryComponent() {
        txtStory.setTitle("Story");

        TextField txtChat = new TextField();
        txtChat.setTitle("Prompt");
        txtChat.addKeyDownListener(Key.ENTER, event -> callPrompt(new Prompt(txtChat.getValue())));
        Button btnSendPrompt = new Button("Send");
        btnSendPrompt.addClickListener(k -> callPrompt(new Prompt(txtChat.getValue())));
        HorizontalLayout hlUserInput = new HorizontalLayout(txtChat, btnSendPrompt);

        return new VerticalLayout(txtStory, hlUserInput);
    }

    private void callPrompt(Prompt prompt) {
        UI current = UI.getCurrent();
        chatModel
                .stream(prompt)
                .subscribe(
                        response ->
                                current.access(
                                        () ->
                                                txtStory.setValue(txtStory.getValue() + response.getResult().getOutput().getText())
                                )
                );
    }
}
