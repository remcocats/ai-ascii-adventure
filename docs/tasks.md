# Improvement Tasks Checklist

Date: 2025-09-28 12:27

Note: Check off items as they are completed. Tasks are ordered from high-level architectural improvements to code-level
refinements, test coverage, and developer experience. Brief notes added for context.

1. [x] Define clear architecture overview in docs (layers, data flow, responsibilities, and component diagram for UI,
   services, AI agents, DTOs, tools) — docs/architecture.md added
2. [x] Introduce module/package boundaries and enforce naming conventions (e.g., api/ui, domain/dto,
   application/services, infrastructure/ai, tools) — boundaries documented via package-info.java files and aligned to
   existing packages
3. [x] Centralize configuration via Spring @ConfigurationProperties (models, endpoints, feature toggles) — AiProperties
   present
4. [ ] Extract AI model and provider selection into strategy/factory pattern (AiModel + resolvers) to decouple ChatView from AiService details
5. [ ] Establish a domain model for Story lifecycle (Story, StoryPart, Character) with invariants and builders to avoid inconsistent states
6. [ ] Define an event-driven flow for story generation (start, delta, completed, error) decoupling UI from AI streaming side-effects
7. [ ] Introduce error handling and user-facing status UI for AI failures and timeouts (retry, cancel, fallback model)
8. [ ] Add logging strategy and correlation IDs for user sessions and AI requests (SLF4J, MDC)
9. [x] Formalize tool/function-calling interfaces with clear contracts and validation (DiceTool, UiCommunicators) —
   @Tool annotations in tools
10. [ ] Create an abstraction for image/ASCII generation providers to support multiple backends (LLaVA, local renderers)
11. [ ] Move prompt templates to dedicated resources with versioning and placeholders; add unit tests for template rendering
12. [ ] Implement persistence for story state (in-memory to start, pluggable storage later) with save/load capability
13. [x] Add concurrency safeguards for UI updates (confirm UI.getCurrent() usage patterns; guard against NPEs and
    detached sessions) — uses UI.access and getUI().ifPresent
14. [ ] Replace direct UI access in callbacks with an event bus or Vaadin UI access scheduler wrapper for testability
15. [ ] Validate user input and sanitize prompts to avoid prompt injection and excessive token usage limits
16. [ ] Introduce rate limiting/debouncing for rapid user inputs in ChatView
17. [ ] Standardize DTOs as immutable records or Lombok value objects where appropriate; ensure equals/hashCode/toString
18. [ ] Document and enforce nullability annotations (@Nullable/@NonNull) and Optional usage guidelines
19. [ ] Replace magic strings ("hero", routes) with enums or constants; centralize in a shared constants class
20. [ ] Add comprehensive unit tests for AiService (streaming, error paths, model selection)
21. [ ] Add unit tests for prompt assembly and post-processing (markdown aggregation, trimming, code block handling)
22. [ ] Add component tests for ChatView using Vaadin TestBench or UI unit tests (send, render, finish)
23. [ ] Add tests for ASCII art/image generation flow and fallback behavior
24. [ ] Add tests for DiceTool and tools’ side-effect boundaries (no UI coupling)
25. [ ] Create contract tests for Agent executors and handoff logic (RouteClassification)
26. [ ] Add integration tests using Testcontainers for Ollama (toggle to stubbed local fake when unavailable)
27. [ ] Introduce linting/static analysis (SpotBugs, Checkstyle) with a baseline and CI enforcement
28. [ ] Configure Maven build profiles for dev/test/prod with sensible defaults (logging, models, timeouts)
29. [ ] Add CI workflow (GitHub Actions) to run build, tests, static analysis, and publish test reports
30. [ ] Add application metrics (timings, counts, success rates) via Micrometer + simple logging registry
31. [ ] Add feature flags to enable/disable experimental features (function-calling, images) per environment
32. [ ] Improve README: quickstart, architecture diagram, configuration table, troubleshooting
33. [ ] Add CONTRIBUTING.md and CODE_OF_CONDUCT.md to onboard contributors
34. [ ] Introduce a CHANGELOG.md with Keep a Changelog format and semantic versioning guidance
35. [x] Implement graceful shutdown and cancellation for in-flight AI requests — AiService.cancelAllActive + @PreDestroy
36. [ ] Add backpressure/flow control for streamed responses to avoid UI lockups and excessive memory use
37. [ ] Encapsulate UI markdown aggregation in a dedicated model that prevents flicker and partial HTML injection
38. [ ] Replace String concatenation for markdown with a StringBuilder or stream-aware buffer
39. [x] Guard against null UI references in async callbacks; add defensive checks and logs — null checks and try/catch
    in UI communicators
40. [ ] Ensure thread naming and executors are configured; consider using Scheduler for reactive streams
41. [x] Extract constants for prompts (CREATE_ASCII_ART_PROMPT_PROMPT, etc.) into a PromptConstants class — implemented
42. [ ] Normalize package structure to avoid duplicate classes under moved paths (ensure tools vs root alignment)
43. [ ] Remove dead/commented code or move to experiments package (e.g., commented image generation code)
44. [ ] Add JavaDoc to public APIs (AiService, agents, DTOs, tools) and key methods
45. [ ] Add serialization safety for DTOs if persisted (version UID, JSON annotations)
46. [ ] Introduce guard rails for token budgeting and truncation with user notice when content is trimmed
47. [ ] Implement retry policy with exponential backoff for transient AI failures
48. [ ] Add centralized exception hierarchy (AiException, PromptTemplateException, UiUpdateException)
49. [ ] Provide a minimal demo dataset and deterministic seed for DiceTool to make tests reproducible
50. [ ] Add dark-mode friendly monospace styling for ASCII art and markdown in UI
