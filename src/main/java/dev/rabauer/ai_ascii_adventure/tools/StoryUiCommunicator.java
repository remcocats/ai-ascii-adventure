package dev.rabauer.ai_ascii_adventure.tools;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import dev.rabauer.ai_ascii_adventure.dto.Npc;
import dev.rabauer.ai_ascii_adventure.dto.Story;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Objects;

@Slf4j
public class StoryUiCommunicator {

    private final Story story;
    private final VerticalLayout verticalLayout;

    public StoryUiCommunicator(Story story, VerticalLayout verticalLayout) {
        this.story = story;
        this.verticalLayout = verticalLayout;
    }

    @Tool(description = "Add a friendly or neutral NPC to the story and render it in the UI. Use this when you introduce a new companion or notable character. The NPC's firstName must be unique.")
    public void addNpc(@ToolParam(description = "the npc") Npc npc) {
        story.npcs().put(npc.getFirstName(), npc);

        verticalLayout.getChildren()
                .filter(component -> {
                            try {
                                return Objects.equals(npc.getFirstName(), component.getElement().getAttribute("firstName"));
                            } catch (Exception e) {
                                return false;
                            }
                        }
                )
                .findFirst()
                .ifPresentOrElse(
                        component -> log.info("Npc {} already exists in story.", npc.getFirstName())
                        , () ->
                                verticalLayout.add(createNpcComponent(npc))
                );

        log.info("Added npc {} to story.", npc.getFirstName());
    }

    private Component createNpcComponent(Npc npc) {
        // Header: Name and role/class
        H4 header = new H4(npc.getName());
        Span meta = new Span("(" + npc.getRace() + " " + npc.getKlass() + ")");
        meta.getElement().setAttribute("theme", "badge small contrast");

        HorizontalLayout summary = new HorizontalLayout(header, meta);
        summary.setAlignItems(HorizontalLayout.Alignment.BASELINE);
        summary.setSpacing(true);

        // Body: stats and lists
        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(false);
        body.setWidthFull();

        // Stats
        Div statsContainer = new Div();
        statsContainer.getStyle().set("display", "grid");
        statsContainer.getStyle().set("grid-template-columns", "1fr 1fr");
        statsContainer.getStyle().set("gap", "var(--lumo-space-m)");

        ProgressBar healthBar = new ProgressBar(0, npc.getMaxHealth(), npc.getHealth());
        Span healthLabel = new Span("Health: " + npc.getHealth() + "/" + npc.getMaxHealth());
        healthLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        ProgressBar manaBar = new ProgressBar(0, npc.getMaxMana(), npc.getMana());
        Span manaLabel = new Span("Mana: " + npc.getMana() + "/" + npc.getMaxMana());
        manaLabel.getStyle().set("font-size", "var(--lumo-font-size-s)");

        statsContainer.add(new Div(healthLabel, healthBar), new Div(manaLabel, manaBar));

        // Inventory
        UnorderedList inventoryList = new UnorderedList();
        inventoryList.getElement().setProperty("title", "Inventory");
        if (npc.getInventory().isEmpty()) {
            inventoryList.add(new ListItem(new Span("(empty)")));
        } else {
            npc.getInventory().forEach(item -> inventoryList.add(new ListItem(new Span(item))));
        }

        // Spell slots
        Span slots = new Span("Spell slots: " + npc.getSpellSlots() + "/" + npc.getMaxSpellSlots());

        body.add(new Span("Role: " + npc.getRole()), statsContainer, slots, new H4("Inventory"), inventoryList);

        Details details = new Details(summary, body);
        details.getElement().setAttribute("firstName", npc.getFirstName());
        details.setOpened(false);
        details.getElement().setAttribute("data-testid", "npc-details");
        return details;
    }

    @Tool(description = "Remove an NPC from the story by first name. Use when an NPC leaves permanently or dies. Does not affect UI elements beyond removal from internal map.")
    public void removeNpc(@ToolParam(description = "NPC first name") String firstName) {
        story.npcs().remove(firstName);
    }

    @Tool(description = "Get the story.")
    public Story getStory() {
        return story;
    }

    @Tool(description = "Get the NPC by its first name.")
    public Npc getNpc(@ToolParam(description = "NPC first name") String firstName) {
        return story.npcs().get(firstName);
    }

    @Tool(description = "Transfer an item from the Hero to an NPC by firstName. Removes from Hero inventory if present and adds to NPC.")
    public void transferItemHeroToNpc(@org.springframework.ai.tool.annotation.ToolParam(description = "item to transfer") String item,
                                      @org.springframework.ai.tool.annotation.ToolParam(description = "target NPC first name") String npcFirstName) {
        Npc npc = story.npcs().get(npcFirstName);
        if (npc == null) return;
        if (story.hero().getInventory().remove(item)) {
            npc.addInventory(item);
        }
    }

    @Tool(description = "Transfer an item from an NPC to the Hero by firstName. Removes from NPC if present and adds to Hero.")
    public void transferItemNpcToHero(@org.springframework.ai.tool.annotation.ToolParam(description = "item to transfer") String item,
                                      @org.springframework.ai.tool.annotation.ToolParam(description = "source NPC first name") String npcFirstName) {
        Npc npc = story.npcs().get(npcFirstName);
        if (npc == null) return;
        if (npc.getInventory().remove(item)) {
            story.hero().addInventory(item);
        }
    }

    @Tool(description = "Transfer an item from one NPC to another by first names.")
    public void transferItemNpcToNpc(@org.springframework.ai.tool.annotation.ToolParam(description = "item to transfer") String item,
                                     @org.springframework.ai.tool.annotation.ToolParam(description = "source NPC first name") String fromFirstName,
                                     @org.springframework.ai.tool.annotation.ToolParam(description = "target NPC first name") String toFirstName) {
        Npc from = story.npcs().get(fromFirstName);
        Npc to = story.npcs().get(toFirstName);
        if (from == null || to == null) return;
        if (from.getInventory().remove(item)) {
            to.addInventory(item);
        }
    }
}
