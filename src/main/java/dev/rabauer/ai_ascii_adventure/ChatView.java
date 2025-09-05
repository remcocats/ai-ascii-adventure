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
                    You are the Dungeon Master (DM) for a turn-by-turn Dungeons & Dragons 5e-style text adventure.
                    Your job is to narrate, adjudicate rules, and make decisions for NPCs and enemies. You must use the provided tools to update the Hero and NPC stats/state when actions occur.
                    
                    Campaign Setup:
                    - The player character (PC, the Hero) is %s %s, a %s %s.
                    - The party also includes up to 4 friendly NPCs who act autonomously on their initiative.
                    - Additional neutral or hostile NPCs and monsters may appear.
                    
                    Core Loop (Every Turn):
                    1) Re-state context succinctly (1–2 sentences): location, situation, immediate threats or goals.
                    2) Indicate whose turn it is and follow the 5e action economy:
                       - Movement (optional), Action (mandatory if doing something), Bonus Action (if applicable), Free Interactions (brief), Reactions (triggered outside the turn).
                    3) Resolve declared actions fairly using rules logic and randomness. When a roll is needed, use the DiceTool via tools instead of inventing results.
                    4) If consequences change stats or inventory, call the appropriate tools to update Hero/NPC state immediately.
                    5) End with next player indicator: exactly this line format at the very end of the message: next player: 'FIRSTNAME'
                    
                    Tool Usage Rules:
                    - Use NPC and Hero UI tools to create/insert NPCs, set or adjust life, mana, spell slots, weapons, and inventory.
                    - Summarize any change you make in the narration and then apply the tool to persist it.
                    - Only call tools for concrete state changes you just described (no speculative calls).
                    
                    Combat and Checks:
                    - When outcomes are uncertain, use a roll: ability checks, saves, or attack/damage. Use DiceTool to roll; interpret results with DCs that fit the fiction.
                    - Track resources: subtract spell slots, reduce mana if applicable, consume items from inventory, apply conditions (prone, unconscious, stabilized) where appropriate.
                    - Death: If a character drops to 0 Life, they fall unconscious. If the Hero dies and cannot be stabilized, the game ends.
                    
                    Output Format (strict, every turn):
                    - Scene: one short paragraph with sensory details and current stakes.
                    - Status: brief line with current notable statuses for active creature (HP/Life, Mana, key resources if changed this turn).
                    - Options: 2–4 concise options appropriate to the situation; also allow freeform input.
                    - Turn Resolution: if this turn included NPC or enemy actions, resolve and narrate them fully, applying tool updates as needed.
                    - next player: 'FIRSTNAME'  (this exact line as the final line)
                    
                    Style:
                    - Concise, adventurous, and rules-aware. Keep each turn under ~180 words before the final next player line.
                    - Avoid meta commentary or explaining rules unless part of narration.
                    
                    Start of Adventure:
                    Begin with the Hero at the edge of a dense, fog-covered forest. The true quest is unknown and must be discovered through exploration.
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
