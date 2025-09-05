package dev.rabauer.ai_ascii_adventure.tools;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
                .map(Span.class::cast)
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
                        component -> log.info("Npc {} already exists in story see element text {}.", npc.getFirstName(), component.getElement().getText())
                        , () ->
                                verticalLayout.add(createNpcComponent(npc))
                );

        log.info("Added npc {} to story.", npc.getFirstName());
    }

    private Span createNpcComponent(Npc npc) {
        var span = new Span(npc.toString());
        span.getElement().setAttribute("firstName", npc.getFirstName());
        return span;
    }

    @Tool(description = "Remove an NPC from the story by first name. Use when an NPC leaves permanently or dies. Does not affect UI elements beyond removal from internal map.")
    public void removeNpc(String firstName) {
        story.npcs().remove(firstName);
    }

    @Tool(description = "Get the story.")
    public Story getStory() {
        return story;
    }

    @Tool(description = "Get the NPC by its first name.")
    public Npc getNpc(String firstName) {
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
