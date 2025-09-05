package dev.rabauer.ai_ascii_adventure.dto;

import java.util.List;
import java.util.Map;


public record Story(List<StoryPart> storyParts, Hero hero, Map<String, Npc> npcs) {

}
