package dev.rabauer.ai_ascii_adventure.ai;

/**
 * Logical model categories used by the app. Concrete model names and providers
 * are configured via AiProperties and wired in Spring, but callers should
 * depend on these roles instead of specific models.
 */
public enum AiModel {
    DEFAULT,
    VISION,
    CLASSIFICATION
}
