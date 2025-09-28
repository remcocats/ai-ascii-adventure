# Testing Plan: Unit tests (JUnit + AssertJ) and Integration tests for AI ASCII Adventure

This document outlines a concrete, incremental plan to introduce reliable automated tests to the project. It focuses on
fast, deterministic unit tests using JUnit 5 + AssertJ and selective Spring Boot integration tests that do not require a
live Ollama server.

## Guiding principles

- Keep tests deterministic and fast (no network calls to Ollama).
- Favor unit tests for logic-heavy classes; use integration tests only for wiring and configuration.
- Use AssertJ for fluent, readable assertions.
- Use Mockito/MockBean to isolate components from external services/models.
- Keep Vaadin UI tests at a smoke-test level to ensure component creation without rendering a browser.

---

## 1) Dependencies and build setup

- Already present: `spring-boot-starter-test` (includes JUnit 5, AssertJ, Mockito).
- No additional dependencies required for the initial scope.
- Ensure tests run with `./mvnw test` and during packaging.

---

## 2) Unit testing scope (JUnit + AssertJ)

Target classes and example assertions:

1. DiceTool (pure logic)
    - Methods: random dice rolls, any helper utilities.
    - Tests:
        - Verify ranges (e.g., d6 ∈ [1,6]) with property-based style loops.
        - If there is seedable RNG or injectable Random, add deterministic test with a fixed seed.
        - AssertJ: `assertThat(result).isBetween(1, 6)`.

2. PromptConstants (prompt text templates)
    - Tests:
        - Validate that formatting placeholders are present and resolved with `String#formatted`.
        - Sanity: not null, contains expected keywords, ends without trailing broken braces.

3. DTOs: Hero, Npc, Story, StoryPart, AbstractCharacter
    - Tests:
        - Basic getters/setters, default values and invariants (e.g., non-negative health/mana if enforced).
        - If Lombok builders exist, verify builder constructs correct instances.
        - Equality/hashCode if applicable.

4. AiService prompt construction and streaming glue (logic level)
    - Private method `constructPrompt` is the core classification prompt builder.
    - Options:
        - Expose `constructPrompt` as package-private for testing; or
        - Verify via public path by mocking the ChatClient interaction.
    - Tests:
        - Given a character name/role and available routes, the prompt contains each route and the character, and
          includes JSON schema instructions.
        - If we keep it private, add a focused test that calls public `generateNewStoryPart(...)` with a stubbed
          classification model and verify that the selected route influences behavior (e.g., specialized prompt choice).
          Use Mockito to stub the classifier call path.

5. AiAsciiAdventureApplication bootstrap switch
    - Tests:
        - Ensure the Netty native transport flag is set to true when `main` runs. Achieve by invoking `main(String[])`
          in an isolated test class and asserting the property value.

---

## 3) Integration testing scope (Spring Boot tests)

Goals:

- Verify Spring context wiring without starting real Ollama.
- Ensure beans of `AiService`, configuration properties, and Vaadin `ChatView` can be constructed with mocked
  dependencies.

Approach:

- Use `@SpringBootTest(classes = AiAsciiAdventureApplication.class)`.
- Use `@MockBean` to override networked components:
    - `ChatModel` beans from Spring AI (default chat and classification model).
    - If a `ChatClient` bean is auto-configured, mock or construct test-friendly instances via
      `AiService#createChatClient`.
- Keep server from starting a real web environment:
  `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` for non-UI integration, and `RANDOM_PORT` only
  if needed.

Tests to add:

1. ContextLoads (existing) — expand
    - Assert that context loads and these beans exist: `AiService`, `AiProperties`, and `ChatView` (if feasible with
      mocks).
    - Example:
        - `assertThat(applicationContext).hasSingleBean(AiService.class);`

2. AiProperties binding
    - Load context with the default `application.properties`.
    - Assert that values are bound correctly, e.g. `ai.defaultModel`, `ai.visionModel`, etc.
    - Use `@Autowired AiProperties` and AssertJ assertions.

3. ChatView smoke (construction + lifecycle)
    - Provide mocked `ChatModel` and a `ChatClient` (built via `AiService#createChatClient(withMemory=false)` using a
      mocked model) to satisfy its constructor.
    - Assert non-null component and that certain UI fields are initialized.
    - Do not open a browser or require Vaadin TestBench.

4. AiService classify route path
    - Configure a mocked classification `ChatModel` and stub its response chain so that
      `entity(RouteClassification.class)` returns a small object with `selectedRoute="npc"`.
    - Call `generateNewStoryPart(...)` with a mocked `ChatClient` that collects prompts; verify the selection influences
      the flow (e.g., the returned `Disposable` gets subscribed and added to internal set). Use `OnNextFunction` to
      collect stream invocations if you provide a minimal stub `ChatClient`.

---

## 4) Test utilities and fakes

- Provide thin fakes for Spring AI interfaces where Mockito is cumbersome:
    - A `FakeChatModel` implementing `ChatModel` that returns a canned response object for classification.
    - A `FakeChatClient` that records prompts and invokes callbacks synchronously.
- Keep these under `src/test/java/.../support` for reuse.

---

## 5) Concrete test files to add

- `src/test/java/dev/rabauer/ai_ascii_adventure/tools/DiceToolTest.java`
    - Range assertions for typical dice methods.

- `src/test/java/dev/rabauer/ai_ascii_adventure/PromptConstantsTest.java`
    - Format and content assertions for key templates.

- `src/test/java/dev/rabauer/ai_ascii_adventure/AiAsciiAdventureApplicationMainTest.java`
    - Verifies Netty flag behavior.

- `src/test/java/dev/rabauer/ai_ascii_adventure/AiPropertiesIntegrationTest.java`
    - `@SpringBootTest` focusing on property binding.

- `src/test/java/dev/rabauer/ai_ascii_adventure/AiServiceUnitTest.java`
    - Unit-level verification of prompt construction and route usage (with fakes/mocks).

- `src/test/java/dev/rabauer/ai_ascii_adventure/ChatViewIntegrationTest.java`
    - `@SpringBootTest` with `@MockBean ChatModel` to allow `ChatView` creation; assert presence in context.

Note: Keep names clear between Unit vs Integration (e.g., suffix `IntegrationTest` for Spring tests) to support future
Maven Surefire/Failsafe split if needed.

---

## 6) Optional: Maven Surefire/Failsafe split

If you want to separate unit vs integration test phases:

- Keep unit tests as `*Test.java` (Surefire, default phase `test`).
- Name integration tests as `*IT.java` and move to Failsafe plugin in the `verify` phase.
- Add to `pom.xml` (optional for now):
    - `maven-surefire-plugin` for unit tests with default includes `**/*Test.java`.
    - `maven-failsafe-plugin` for integration tests with includes `**/*IT.java`.

Given current scope, this is optional. Start simple and split later when needed.

---

## 7) CI considerations

- Basic GitHub Actions/JetBrains Space template to run `./mvnw -B -ntp -DskipITs=false test`.
- Cache Maven repo for speed.
- Add matrix only if needed (JDK versions).

---

## 8) Execution order for implementation

1. Add simple unit tests for DiceTool and PromptConstants.
2. Add property binding integration test for `AiProperties` with AssertJ assertions.
3. Add `ChatView` integration smoke test with `@MockBean ChatModel`.
4. Add `AiService` unit tests for prompt construction/route selection using fakes.
5. Optionally, introduce Failsafe split if integration tests grow.

---

## 9) Notes and risks

- Avoid real calls to Ollama — always mock/stub `ChatModel` and/or `ChatClient`.
- Vaadin UI rendering is not exercised; tests verify construction and basic field state only.
- If `AiService.classifyInquiry` remains private, test via the public flow or refactor to package-private.
- Streaming behavior can be tested synchronously by faking `ChatClientResponse` publisher to invoke callbacks.
