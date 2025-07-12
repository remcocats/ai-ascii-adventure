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
               Generate an interactive fantasy text adventure game starring a hero named %s. The game should be played turn by turn, with each turn offering a description of the current scene and allowing the player to choose what Hero does next.
                       
                       Game Rules:
                       
                           Hero is a brave hero exploring a mysterious fantasy world filled with magic, monsters, and secrets.
                       
                           Hero has the following stats:
                       
                               Life: 100 (if it reaches 0, Hero dies and the game ends)
                       
                               Mana: 50 (used to cast spells or perform magical actions)
                       
                               Inventory: Starts empty but can be filled with items, weapons, potions, artifacts, etc.
                       
                           Each turn, describe:
                       
                               The current location and atmosphere
                       
                               Any characters, enemies, items, or mysteries present
                       
                               Hero’ current status (life, mana, inventory)
                       
                               Present 2–4 clear choices for the player, or allow freeform input
                       
                           The player can input actions like:
                       
                               "Attack the goblin"
                       
                               "Search the chest"
                       
                               "Cast Firebolt"
                       
                               "Drink a health potion"
                       
                               "Run away"
                       
                               Or make decisions like "Go north" or "Talk to the wizard"
                       
                           Keep track of Hero' health, mana, and inventory throughout the adventure.
                       
                           Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                       
                           The adventure should be completed after approximately 15 turns, though it can be shorter or longer depending on the path taken.
                       
                           Ensure a satisfying ending (victory, defeat, or an ambiguous fate) based on how the story unfolds.
                       
                       Tone and Setting:
                       
                           Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures.
                       
                           Tone should be adventurous and mysterious, with occasional moments of danger or humor.
                       
                       First step:
                       Begin the story with Hero standing at the edge of a dense, fog-covered forest. His quest is unknown — he must discover it as he explores. 
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
        txtStory.setSizeFull();

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
