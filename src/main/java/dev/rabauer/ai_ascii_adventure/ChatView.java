package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import dev.rabauer.ai_ascii_adventure.config.AiProperties;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import dev.rabauer.ai_ascii_adventure.dto.Story;
import dev.rabauer.ai_ascii_adventure.dto.StoryPart;
import dev.rabauer.ai_ascii_adventure.tools.DiceTool;
import dev.rabauer.ai_ascii_adventure.tools.HeroUiCommunicator;
import dev.rabauer.ai_ascii_adventure.tools.NpcUiCommunicator;
import dev.rabauer.ai_ascii_adventure.tools.StoryUiCommunicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

@Route(value = "", layout = MainLayout.class)
public class ChatView extends SplitLayout implements GameManager {

    private static final Logger LOG = LoggerFactory.getLogger(ChatView.class);

    private final Markdown markdownStory = new Markdown();
    private final TextArea txtAsciiArt = new TextArea();

    private final ChatClient chatClient;
    private final AiService aiService;
    private final ChatModel visionChatModel;
    private final AiProperties aiProperties;
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
    public ChatView(AiService aiService, ChatClient chatClient, ChatModel visionChatModel, AiProperties aiProperties) {
        this.aiService = aiService;
        this.chatClient = chatClient;
        this.visionChatModel = visionChatModel;
        this.aiProperties = aiProperties;

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

            // Initialize UI with hero's current attributes
            this.heroCommunicator.setHealthHero(hero.getHealth());
            this.heroCommunicator.setManaHero(hero.getMana());
            this.heroCommunicator.setSpellSlotsHero(hero.getSpellSlots());
            this.heroCommunicator.updateInventory();
            this.heroCommunicator.updateWeapons();

            generateNewStoryPart(hero.getRole(), PromptConstants.INITIAL_PROMPT.formatted(hero.getFirstName(), hero.getLastName(), hero.getRace(), hero.getKlass()));
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
        // ASCII Map area (read-only, monospace)
        txtAsciiArt.setSizeFull();
        txtAsciiArt.setReadOnly(true);
        txtAsciiArt.setLabel("ASCII Map");
        txtAsciiArt.getStyle().set("font-family", "'Courier New', monospace");

        // Status bars with accessible labels
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
        hlStatusBar.setSpacing(true);
        hlStatusBar.setPadding(false);
        hlStatusBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        hlStatusBar.setFlexGrow(1, vlHealth, vlMana, vlSpellSlots);

        // Inventory and Weapons areas wrapped in Scrollers to avoid fixed heights
        spnInventory = new Span();
        spnInventory.setWidthFull();
        Scroller inventoryScroller = new Scroller(spnInventory);
        inventoryScroller.setSizeFull();
        inventoryScroller.getElement().setAttribute("aria-label", "Inventory list");
        VerticalLayout inventorySection = new VerticalLayout(new H3("Inventory"), inventoryScroller);
        inventorySection.setPadding(false);
        inventorySection.setSpacing(false);
        inventorySection.setSizeFull();

        spnWeapons = new Span();
        spnWeapons.setWidthFull();
        Scroller weaponsScroller = new Scroller(spnWeapons);
        weaponsScroller.setSizeFull();
        weaponsScroller.getElement().setAttribute("aria-label", "Weapons list");
        VerticalLayout weaponsSection = new VerticalLayout(new H3("Weapons"), weaponsScroller);
        weaponsSection.setPadding(false);
        weaponsSection.setSpacing(false);
        weaponsSection.setSizeFull();

        VerticalLayout statsSection = new VerticalLayout(new H3("Status"), hlStatusBar);
        statsSection.setPadding(false);
        statsSection.setSpacing(false);
        statsSection.setWidthFull();

        VerticalLayout right = new VerticalLayout(txtAsciiArt, statsSection, inventorySection, weaponsSection);
        right.setSizeFull();
        right.setSpacing(true);
        right.setPadding(true);
        right.setFlexGrow(1, txtAsciiArt);
        right.setFlexGrow(0, statsSection);
        right.setFlexGrow(1, inventorySection, weaponsSection);
        return right;
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
        try {
            if (inFlightStory != null && !inFlightStory.isDisposed()) {
                inFlightStory.dispose();
            }
        } catch (Exception e) {
            LOG.warn("Failed disposing inFlightStory", e);
        } finally {
            inFlightStory = null;
        }
        try {
            if (inFlightAscii != null && !inFlightAscii.isDisposed()) {
                inFlightAscii.dispose();
            }
        } catch (Exception e) {
            LOG.warn("Failed disposing inFlightAscii", e);
        } finally {
            inFlightAscii = null;
        }
    }

    private void generateNewStoryPart(String character, String textPrompt) {
        var currentSession = VaadinSession.getCurrent();

        if (currentSession != null) {
            currentSession.access(() -> this.markdownStory.setContent(""));
        }

        UI current = UI.getCurrent();
        cancelInFlight();
        inFlightStory = aiService.generateNewStoryPart(
                dev.rabauer.ai_ascii_adventure.ai.AiModel.DEFAULT,
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

        String firstName = extractFirstNameFrom(storyPartAsString);

        if (!firstName.equalsIgnoreCase(heroCommunicator.getHero().getFirstName())) {
            String options = extractOptionsFrom(storyPartAsString);

            Mono.delay(Duration.ofSeconds(aiProperties.npcDecisionDelaySeconds())).subscribe(ignored -> {
                try {
                    generateNewStoryPart(firstName, """
                            Decide for the NPC what to do next. Based on the following options %s, choose one.
                            """.formatted(options));
                } catch (Exception ex) {
                    LOG.error("Failed to trigger NPC decision for {}", firstName, ex);
                }
            });
        }
        createAsciiArt(storyPartAsString);

//        aiService.generatePicture(
//                aiService.createChatClient(false, AiModel.OLLAMA_LLAVA.model),
//                CREATE_IMAGE_PROMPT_PROMPT.formatted(storyPartAsString),
//                response -> {
//                }
//        );
    }

    private String extractOptionsFrom(String storyPartAsString) {
        if (storyPartAsString == null) return "";
        String lower = storyPartAsString;
        int idxOptions = lower.indexOf("Options");
        if (idxOptions < 0) return "";
        int idxNext = lower.indexOf("next player", idxOptions);
        String section = idxNext > idxOptions ? lower.substring(idxOptions, idxNext) : lower.substring(idxOptions);
        // Remove leading label and punctuation
        section = section.replaceFirst("(?is)^Options *:?", "");
        return section.trim();
    }

    private String extractFirstNameFrom(String storyPartAsString) {
        if (storyPartAsString == null) return "";
        String s = storyPartAsString;
        int idx = s.indexOf("next player");
        if (idx >= 0) {
            String tail = s.substring(idx);
            int colon = tail.indexOf(":");
            if (colon >= 0) {
                String after = tail.substring(colon + 1).trim();
                // strip quotes and trailing content
                after = after.replace("\n", " ").trim();
                if (after.startsWith("'")) {
                    int end = after.indexOf("'", 1);
                    if (end > 1) {
                        return after.substring(1, end).trim();
                    }
                }
                if (after.startsWith("\"")) {
                    int end = after.indexOf("\"", 1);
                    if (end > 1) {
                        return after.substring(1, end).trim();
                    }
                }
                // fallback: take first token
                String[] parts = after.split("\\s+");
                if (parts.length > 0) return parts[0].replace("'", "").replace("\"", "").trim();
            }
        }
        // legacy fallback
        String[] split = storyPartAsString.split("next player");
        String firstName = split[split.length - 1];
        return firstName.replace(":", "").replace("\n", "").replace("'", "").replace("\"", "").trim();
    }

    private void createAsciiArt(String storyPartAsString) {
        UI current = UI.getCurrent();
        inFlightAscii = aiService.generateAsciiArt(
                dev.rabauer.ai_ascii_adventure.ai.AiModel.VISION,
                PromptConstants.CREATE_ASCII_ART_PROMPT_PROMPT.formatted(storyPartAsString),
                response -> current.access(() -> txtAsciiArt.setValue(response))
        );
    }

}
