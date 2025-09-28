package dev.rabauer.ai_ascii_adventure.dto;

import java.util.Objects;

public record StoryPart(String character, String text) {
    public StoryPart {
        character = requireText(character, "character");
        text = requireText(text, "text");
    }

    public static StoryPart of(String character, String text) {
        return new StoryPart(character, text);
    }

    private static String requireText(String v, String name) {
        String s = Objects.requireNonNull(v, name).trim();
        if (s.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return s;
    }
}
