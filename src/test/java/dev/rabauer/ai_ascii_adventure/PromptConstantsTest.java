package dev.rabauer.ai_ascii_adventure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptConstantsTest {

    @Test
    void initialPrompt_hasPlaceholders_andFormatsCorrectly() {
        String templ = PromptConstants.INITIAL_PROMPT;
        assertThat(templ).isNotNull();
        assertThat(templ).contains("%s %s, a %s %s");
        String formatted = templ.formatted("Hans", "The Concur", "Elf", "Barbarian");
        assertThat(formatted)
                .contains("Hans The Concur, a Elf Barbarian")
                .contains("next player:")
                .doesNotContain("%s");
        // sanity: markdown sections
        assertThat(formatted).contains("# Turn Structure");
        assertThat(formatted.trim()).endsWith("Neverwinter, at a square close to the house that he owned, in Spring. The true quest is unknown and must be discovered through exploration.");
    }

    @Test
    void createImagePrompt_formats_text() {
        String templ = PromptConstants.CREATE_IMAGE_PROMPT_PROMPT;
        String text = "A dawn over mountains";
        String formatted = templ.formatted(text);
        assertThat(formatted).contains(text);
        assertThat(formatted).doesNotContain("%s");
    }

    @Test
    void createAsciiPrompt_formats_text() {
        String templ = PromptConstants.CREATE_ASCII_ART_PROMPT_PROMPT;
        String story = "The river bends near ruins";
        String formatted = templ.formatted(story);
        assertThat(formatted).contains(story);
        assertThat(formatted).contains("Return only the ASCII map");
        assertThat(formatted).doesNotContain("%s");
    }
}
