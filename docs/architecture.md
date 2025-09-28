# Architecture Overview

Date: 2025-09-28

This document describes the current architecture of the AI ASCII Adventure application. It covers the main layers, data
flow, responsibilities, and the key components: UI, services, AI agents, DTOs, and tools.

Goals:

- Keep UI responsive and safe in presence of asynchronous AI streaming
- Encapsulate domain state (Hero, NPCs, Story)
- Provide tool/function interfaces that the AI can call deterministically
- Allow evolution of AI model selection and providers over time

Layers and Packages

- UI (api/ui): Vaadin views and layout
    - ChatView (Route "") is the main user interface
    - Renders: Story markdown, hero panel, NPC list
    - Handles user prompts and cancel actions
- Application/Services (application/services): coordination and orchestration
    - AiService: wraps Spring AI ChatClient usage (streaming, classification, ascii generation)
    - Provides cancellation and lifecycle management (@PreDestroy)
- Domain/DTO (domain/dto): state and model
    - dto.AbstractCharacter, dto.Hero, dto.Npc, dto.Story, dto.StoryPart
    - Immutable-ish through Lombok data with controlled update methods
- Tools (tools): callable functions exposed to the LLM
    - HeroUiCommunicator, NpcUiCommunicator, StoryUiCommunicator, DiceTool
    - Annotated with @Tool, parameters described with @ToolParam
- Infrastructure/AI (infrastructure/ai): AI-specific helpers and models
    - ai.RouteClassification (LLM JSON mapping)
    - Config: config.AiProperties (@ConfigurationProperties)

High-Level Data Flow

1) Start Game

- ChatView prompts for Hero info
- Constructs domain objects (Hero, Story)
- Instantiates communicators (tools) bound to current UI components

2) User Prompt / Turn Progression

- User enters text in ChatView; generateNewStoryPart is invoked
- AiService.generateNewStoryPart streams assistant tokens via ChatClient
- ChatView appends tokens to Markdown component inside UI.access
- On stream completion, ChatView parses the turn result (options, next player)
- If next player is an NPC, ChatView triggers an NPC decision after a small delay

3) Tool Calling

- During streaming, the model may call tools (e.g., update hero health, add NPC)
- Tools update domain state and UI in a thread-safe way using getUI().ifPresent and UI.access

4) ASCII Map Generation

- After a turn completes, ChatView calls AiService.generateAsciiArt with the story text using a map-focused prompt (
  PromptConstants)
- The returned ASCII is shown in a side panel (planned/partial)

5) Cancellation and Shutdown

- ChatView can cancel in-flight disposables (user pressed Cancel or view detached)
- AiService tracks active Disposables and cancels all on shutdown (@PreDestroy)

Responsibilities

- ChatView: UI rendering, user input handling, token aggregation, completion handling, triggering NPC turns
- AiService: AI calls, streaming, classification routing, backpressure/cancellation hooks
- Tools (Hero/Npc/Story): Domain updates and UI synchronization; transfer of items; basic validation
- DTOs: Represent characters, story parts, and collections with simple invariants (e.g., clamped stats handled at call
  sites)
- Config (AiProperties): Model choices and runtime tuning knobs

Component Diagram (textual)
[Browser/UI (Vaadin)]
-> ChatView
-> (uses) AiService
-> (owns) HeroUiCommunicator, NpcUiCommunicator, StoryUiCommunicator, DiceTool
-> (renders) Markdown, NPC layout, Hero bars/spans

[Spring AI]
<- ChatClient (from ChatModel beans)
<- Tools registry (Hero/Npc/Story/Dice annotated)

[Domain]
<- Story { Hero, Map<String,Npc>, List<StoryPart> }
<- Hero extends AbstractCharacter
<- Npc extends AbstractCharacter

Threading and Safety

- All UI mutations are done inside UI.access or via getUI().ifPresent checks to avoid NPEs and detached-session errors
- AiService maintains a concurrent set of active Disposables; cancellation is idempotent with error guards

Known Gaps and Next Steps

- Clarify module boundaries in code structure (naming already suggests intent)
- Introduce event-driven story flow to decouple UI from streaming callbacks
- Strategy/factory for model/provider selection
- Backpressure for streamed responses
- Persistence for story state
