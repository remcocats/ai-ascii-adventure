package dev.rabauer.ai_ascii_adventure.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Story(List<StoryPart> storyParts, Hero hero, Map<String, Npc> npcs) {
    public Story {
        Objects.requireNonNull(storyParts, "storyParts");
        Objects.requireNonNull(hero, "hero");
        Objects.requireNonNull(npcs, "npcs");
    }
}
