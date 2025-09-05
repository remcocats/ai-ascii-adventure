package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import dev.rabauer.ai_ascii_adventure.dto.Story;
import dev.rabauer.ai_ascii_adventure.dto.StoryPart;
import dev.rabauer.ai_ascii_adventure.models.AiModel;
import dev.rabauer.ai_ascii_adventure.tools.DiceTool;
import dev.rabauer.ai_ascii_adventure.tools.HeroUiCommunicator;
import dev.rabauer.ai_ascii_adventure.tools.NpcUiCommunicator;
import dev.rabauer.ai_ascii_adventure.tools.StoryUiCommunicator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.HashMap;

@Route(value = "", layout = MainLayout.class)
public class ChatView extends SplitLayout implements GameManager {

    private static final String INITIAL_PROMPT =
            """
                       You are a dungeon master, and decide what happens in this game. You update NPC's and Hero statics with tool that are at your disposal.
                       Generate an interactive fantasy text adventure game, in the style of dungeons and dragons, starring a hero named %s %s and his 4 friends.
                       His friends are NPC's and the hero is the player. The NPC's are controlled by agents.
                       The game should be played turn by turn, with each turn offering a description of the current scene and allowing the player or the NPC to choose what it does next.
                               Game Rules:
                                   - The game should be a dungeon adventure, with a dungeon-like environment and a storyline.
                                   - The game rules should be based on dungeons and dragons 5th edition.
                                   - The game is a turn by turn game, with each turn offering a description of the current scene and allowing the player or the agent to choose what next player does next.
                                   - If there are NPC's in the story you will use a tool to put the NPC in the story and use tools to interact with the NPC fill in everything you know about the NPC and update the stats during the story.
                                   - Hero is a brave hero exploring a mysterious fantasy world filled with magic, monsters, and secrets based on dungeons and dragons 5th edition.
                                   - Hero has the race %s and the class of %s
                                   - Hero and NPC's has the following stats:
                                       * Life: 100 (if it reaches 0, Hero dies and the game ends)
                                       * Mana: 50 (used to cast spells or perform magical actions) or 0 if his race or class can't cast spells
                                       * Spell slots: that is part of his type and class
                                       * Weapons: that is part of his type and class
                                       * Inventory: Starts with the base part of his race and class, but can be filled with items, weapons, potions, artifacts, etc.
                                   - Each turn, describe:
                                       * The current location and atmosphere
                                       * Any characters, enemies, items, or mysteries present
                                       * Hero’ current status (life, mana, inventory, spell slots, weapons)
                                       * Present 2–4 clear choices for the player, or allow freeform input
                                       * And tell who is the next on turn, the hero or one of the NPC's, place the answer always at the end. With the format "next player: 'player firstName'"
                                   - The player can input actions like:
                                       * "Attack the goblin"
                                       * "Search the chest"
                                       * "Cast Firebolt"
                                       * "Drink a health potion"
                                       * "Run away"
                                       * "Talk with friends"
                                       * Or make decisions like "Go north" or "Talk to the wizard"
                                   - Keep track of Hero' health, mana, weapons, spell slots and inventory throughout the adventure.
                                   - Let the story unfold based on the player’s choices, with real consequences (combat, traps, treasures, allies, etc.).
                                   - The adventure should be completed after approximately 20 turns, though it can be shorter or longer depending on the path taken.
                                   - Ensure a satisfying ending (victory, defeat, or an ambiguous fate) based on how the story unfolds.
                               
                               Tone and Setting:
                                   - Classic high-fantasy world: enchanted forests, forgotten ruins, ancient magic, mythical creatures and other things of dungeons and dragons.
                                   - Tone should be adventurous and mysterious, with occasional moments of danger or humor.
                               
                               First step:
                               Begin the story with Hero standing at the edge of a dense, fog-covered forest. His quest is unknown — he must discover it as he explores.
                    """;

    private final static String CREATE_IMAGE_PROMPT_PROMPT = """
            Given the following passage of text, extract its quintessence — the single most essential concept, 
            emotion, or idea it conveys. Then, write a short, vivid prompt for generating a clear, 
            minimal image that visually represents that essence. The image should be easy to understand, 
            containing only the most necessary elements to express the idea, with no clutter or complex 
            scenery. Avoid metaphor unless it is visually obvious. Focus on simplicity and clarity, 
            suitable for both humans and AI to grasp at a glance.
                        
            Text:
            %s
            """;

    private final static String CREATE_ASCII_ART_PROMPT_PROMPT = """
                Read the following text, extract its core meaning, and create a simple, beautiful, and recognizable ASCII art representation of it. Use minimal characters and clean lines.
                
                Important: Return only the ASCII art — no explanation, no commentary, no labels. The output must consist of ASCII characters only.
                
                Text:
                %s
            """;

    private final Markdown markdownStory = new Markdown();
    private final TextArea txtAsciiArt = new TextArea();

    private final ChatClient chatClient;
    private final AiService aiService;
    private ProgressBar prbHealth;
    private ProgressBar prbMana;
    private ProgressBar prbSpellSlots;
    private HeroUiCommunicator heroCommunicator;
    private NpcUiCommunicator npcCommunicator;
    private StoryUiCommunicator storyCommunicator;
    private DiceTool diceTool;
    private Span spnInventory;
    private Span spnWeapons;
    private Story story;

    private final VerticalLayout npcVerticalLayout = new VerticalLayout();

    private Disposable inFlightStory;
    private Disposable inFlightAscii;

    @Autowired
    public ChatView(AiService aiService) {
        this.aiService = aiService;
        this.chatClient = aiService.createChatClient(true, AiModel.OLLAMA_GPT_OSS.model);

        this.setSizeFull();
        this.addToPrimary(createPrimaryPartHeroAndNpc());
        this.addToSecondary(createSecondaryPartTheStory());

        openStartDialog();

        this.addDetachListener(e -> cancelInFlight());
    }

    private void openStartDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        dialog.setHeaderTitle("Input Hero name");

        TextField txtHeroName = new TextField("Hero name");
        txtHeroName.setValue("Hans");
        dialog.add(txtHeroName);

        TextField txtHereLastName = new TextField("Last name");
        txtHereLastName.setValue("The Concur");
        dialog.add(txtHereLastName);

        TextField txtHeroType = new TextField("Type");
        txtHeroType.setValue("Elf");
        dialog.add(txtHeroType);

        TextField txtHeroClass = new TextField("Class");
        txtHeroClass.setValue("Barbarian");
        dialog.add(txtHeroClass);

        Button saveButton = new Button("Ok");
        saveButton.addClickListener(buttonClickEvent ->
        {
            dialog.close();
            Hero hero = new Hero(txtHeroName.getValue(), txtHereLastName.getValue(), txtHeroType.getValue(), txtHeroClass.getValue());
            this.heroCommunicator = new HeroUiCommunicator(
                    hero, this.prbHealth, this.prbMana, this.prbSpellSlots, this.spnInventory, this.spnWeapons, this
            );
            this.npcCommunicator = new NpcUiCommunicator(this.npcVerticalLayout);

            this.story = new Story(new ArrayList<>(), hero, new HashMap<>());

            this.storyCommunicator = new StoryUiCommunicator(story, this.npcVerticalLayout);

            this.diceTool = new DiceTool();

            generateNewStoryPart(hero.getRole(), INITIAL_PROMPT.formatted(hero.getFirstName(), hero.getLastName(), hero.getRace(), hero.getKlass()));
        });
        dialog.getFooter().add(saveButton);
        dialog.open();
    }

    public void showGameOver(boolean fail) {
        UI.getCurrent().access(() ->
        {
            Dialog dialog = new Dialog();
            dialog.setCloseOnOutsideClick(false);
            dialog.setCloseOnEsc(false);

            if (fail) {
                dialog.setHeaderTitle("Game Over");
                dialog.add(new Span("You died. Try again soon."));
            } else {
                dialog.setHeaderTitle("Victory!");
                dialog.add(new Span("You finished the game! Congratulations!"));
            }
            dialog.open();
        });
    }

    private SplitLayout createPrimaryPartHeroAndNpc() {
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        splitLayout.addToPrimary(createSecondaryPartNpcs());
        splitLayout.addToSecondary(createPrimaryPartAsciArtAndHero());
        return splitLayout;
    }

    private Component createSecondaryPartNpcs() {
        npcVerticalLayout.setSizeFull();
        npcVerticalLayout.setSpacing(true);
        npcVerticalLayout.setPadding(true);
        npcVerticalLayout.addComponentAsFirst(new Span("NPCs"));
        return npcVerticalLayout;
    }

    private Component createPrimaryPartAsciArtAndHero() {
        txtAsciiArt.setSizeFull();
        txtAsciiArt.setTitle("Graphics");
        txtAsciiArt.getStyle().set("font-family", "'Courier New', monospace");

        prbHealth = new ProgressBar(0, 1, 1);
        prbHealth.addThemeVariants(ProgressBarVariant.LUMO_CONTRAST);
        prbHealth.setWidthFull();
        prbHealth.setHeight("20px");
        NativeLabel lblHealth = new NativeLabel("Health");
        lblHealth.setId("pbHealthLbl");
        prbHealth.getElement().setAttribute("aria-labelledby", "pbHealthLbl");
        VerticalLayout vlHealth = new VerticalLayout(lblHealth, prbHealth);

        prbMana = new ProgressBar(0, 1, 0.5);
        prbMana.addThemeVariants(ProgressBarVariant.LUMO_ERROR);
        prbMana.setWidthFull();
        prbMana.setHeight("20px");
        NativeLabel lblMana = new NativeLabel("Mana");
        lblMana.setId("pbManaLbl");
        prbMana.getElement().setAttribute("aria-labelledby", "pbManaLbl");
        VerticalLayout vlMana = new VerticalLayout(lblMana, prbMana);

        prbSpellSlots = new ProgressBar(0, 1, 0.5);
        prbSpellSlots.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        prbSpellSlots.setWidthFull();
        prbSpellSlots.setHeight("20px");
        NativeLabel lblSpellSlots = new NativeLabel("Spell slots");
        lblSpellSlots.setId("pbSpellSlotsLbl");
        prbSpellSlots.getElement().setAttribute("aria-labelledby", "pbSpellSlotsLbl");
        VerticalLayout vlSpellSlots = new VerticalLayout(lblSpellSlots, prbSpellSlots);

        HorizontalLayout hlStatusBar = new HorizontalLayout(vlHealth, vlMana, vlSpellSlots);
        hlStatusBar.setWidthFull();

        spnInventory = new Span();
        spnInventory.setWidthFull();
        spnInventory.setHeight("100px");
        HorizontalLayout hlInventory = new HorizontalLayout(spnInventory);
        hlInventory.setHeight("100px");
        hlInventory.setWidthFull();

        spnWeapons = new Span();
        spnWeapons.setWidthFull();
        spnWeapons.setHeight("100px");
        HorizontalLayout hlWeapons = new HorizontalLayout(spnWeapons);
        hlWeapons.setHeight("100px");
        hlWeapons.setWidthFull();

        return new VerticalLayout(txtAsciiArt, hlStatusBar, hlInventory, hlWeapons);
    }

    private Component createSecondaryPartTheStory() {
        markdownStory.setSizeFull();
        markdownStory.getStyle().set("overflow-y", "auto");
        markdownStory.setHeightFull();

        TextField txtChat = new TextField();
        txtChat.setTitle("Prompt");
        txtChat.addKeyDownListener(Key.ENTER, event -> {
            generateNewStoryPart("hero", txtChat.getValue());
            txtChat.clear();
        });
        Button btnSendPrompt = new Button("Send");
        btnSendPrompt.addClickListener(k -> {
            generateNewStoryPart("hero", txtChat.getValue());
            txtChat.clear();
        });
        Button btnCancel = new Button("Cancel");
        btnCancel.addClickListener(k -> cancelInFlight());
        HorizontalLayout hlUserInput = new HorizontalLayout(txtChat, btnSendPrompt, btnCancel);

        return new VerticalLayout(markdownStory, hlUserInput);
    }

    private void cancelInFlight() {
        if (inFlightStory != null && !inFlightStory.isDisposed()) {
            inFlightStory.dispose();
        }
        if (inFlightAscii != null && !inFlightAscii.isDisposed()) {
            inFlightAscii.dispose();
        }
    }

    private void generateNewStoryPart(String character, String textPrompt) {
        this.markdownStory.setContent("");

        UI current = UI.getCurrent();
        cancelInFlight();
        inFlightStory = aiService.generateNewStoryPart(
                this.chatClient,
                character,
                textPrompt,
                response ->
                {
                    if (current != null) {
                        current.access(
                                () ->
                                {
                                    markdownStory.setContent(
                                            markdownStory.getContent() +
                                                    response.chatResponse()
                                                            .getResult()
                                                            .getOutput()
                                                            .getText()
                                    );
                                }
                        );
                    }
                }, () -> {
                    if (current != null) {
                        current.access(() -> {
                            handleFinishedStoryPart(character, markdownStory.getContent());

                            var now = npcVerticalLayout.getChildren().toList();
                            npcVerticalLayout.removeAll();
                            npcVerticalLayout.add(now);
                        });
                    }
                },
                heroCommunicator,
                npcCommunicator,
                storyCommunicator,
                diceTool
        );

    }

    private void handleFinishedStoryPart(String character, String storyPartAsString) {
        StoryPart storyPart = new StoryPart(character, storyPartAsString);
        this.story.storyParts().add(storyPart);

        UI current = UI.getCurrent();
        inFlightAscii = aiService.generateAsciiArt(
                aiService.createChatClient(false, AiModel.OLLAMA_LLAVA.model),
                CREATE_ASCII_ART_PROMPT_PROMPT.formatted(storyPartAsString),
                response -> current.access(() -> txtAsciiArt.setValue(response))
        );

//        aiService.generatePicture(
//                aiService.createChatClient(false, AiModel.OLLAMA_LLAVA.model),
//                CREATE_IMAGE_PROMPT_PROMPT.formatted(storyPartAsString),
//                response -> {
//
//                }
//        );
    }

}
