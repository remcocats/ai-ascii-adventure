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

    @Tool(description = "Add a NPC to the story.")
    public void addNpc(@ToolParam(description = "the npc") Npc npc) {
        story.npcs().put(npc.getFirstName(), npc);

        verticalLayout.getChildren()
                .map(Span.class::cast)
                .filter(component -> {
                            try {
                                Objects.equals(npc.getFirstName(), component.getElement().getAttribute("firstName"));
                                return false;
                            } catch (Exception e) {
                                return true;
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
        var span = new Span(npc.getName());
        span.getElement().setAttribute("firstName", npc.getFirstName());
        return span;
    }

    @Tool(description = "Remove a NPC from the story based on his first name.")
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
}
