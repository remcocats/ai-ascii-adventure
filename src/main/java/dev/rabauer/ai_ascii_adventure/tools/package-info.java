@ParametersAreNonnullByDefault
package dev.rabauer.ai_ascii_adventure.tools;

/**
 * Layer: Tools (tools)
 * <p>
 * Contains @Tool-annotated callable interfaces the LLM can invoke to affect domain state
 * and reflect changes in the UI in a controlled manner.
 * - HeroUiCommunicator, NpcUiCommunicator, StoryUiCommunicator, DiceTool
 * <p>
 * Guidelines:
 * - Perform input validation and clamp values where applicable.
 * - Avoid heavy business logic; delegate to services if needed.
 * - UI updates must use Vaadin UI access patterns and null-safe checks.
 */

import javax.annotation.ParametersAreNonnullByDefault;
