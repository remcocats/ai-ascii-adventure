@ParametersAreNonnullByDefault
package dev.rabauer.ai_ascii_adventure.dto;

/**
 * Layer: Domain/DTO (domain/dto)
 * <p>
 * Contains the domain model types representing Story state and characters.
 * - AbstractCharacter, Hero, Npc, Story, StoryPart
 * <p>
 * Guidelines:
 * - Keep data-focused and side-effect free; mutations should be minimal and validated at call sites.
 * - Avoid UI dependencies. No Vaadin imports here.
 */

import javax.annotation.ParametersAreNonnullByDefault;
