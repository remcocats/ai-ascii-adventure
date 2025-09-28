@ParametersAreNonnullByDefault
package dev.rabauer.ai_ascii_adventure;

/**
 * Layer: UI (api/ui)
 * <p>
 * Contains Vaadin view/layout classes that comprise the user interface and app entry wiring.
 * Currently includes:
 * - ChatView: main game UI and interaction surface
 * - MainLayout: top-level layout used by routes
 * <p>
 * Notes on boundaries:
 * - UI code should not contain business logic; delegate to services (AiService) and tools.
 * - All UI mutations must happen inside Vaadin UI.access or via getUI().ifPresent.
 * - Prefer interacting with domain via communicators/tools.
 */

import javax.annotation.ParametersAreNonnullByDefault;
