# Working Preferences

## Coding Standards

- **Formatter**: Google Java Format (GOOGLE style) via Spotless
  - Apply before committing: `mvn spotless:apply`
  - Full clean build: `mvn clean spotless:apply package`
  - Spotless runs automatically at compile phase (`mvn compile` will fail if unformatted)
- **Java version**: 21 (use records, sealed classes, pattern matching where appropriate)
- **License header**: Every Java file must start with `/* Copyright (C) The Authors $YEAR */` — Spotless enforces this
- **Architecture**: Loose ECS — entities as Java objects with components; pragmatic, not dogmatic
- **Naming**: Follow Google Java Style (camelCase methods/fields, PascalCase classes, UPPER_SNAKE constants)
- **No magic numbers**: See `MAGIC_NUMBERS_CATALOG.md` for catalogued constants; extract new ones with named constants

## Testing Standards

- **Framework**: JUnit Jupiter 5 + Mockito 5
- **Coverage**: High test coverage expected; JaCoCo is configured
- **Headless tests**: All game-logic and collision tests MUST run without an OpenGL/GLFW context
  - Follow the pattern in `TestPlayerCollision`, `TestPlayerCollisionPassing`, `TestRooms`, `TestStage`
  - Use the shared `src/test/java/abbaye/model/Utils.java` test helper
- **Test resources**: Minimal map data lives in `src/test/resources/test-map.txt`
- Do not add tests that require a display or GPU context

## AI Interaction Preferences

- **Scope**: Stick strictly to what is asked — no unsolicited refactors, feature suggestions, or clean-ups of unrelated code
- **Explanations**: Provide explanations for major or non-obvious changes; skip boilerplate commentary on trivial edits
- **Assumed knowledge**: Expert-level Java and software engineering — no need to explain language basics or well-known patterns
- **Minimal diffs**: Produce the smallest change that solves the problem
- **No speculative code**: Do not add error handling, abstractions, or features not explicitly requested

## Communication Preferences

- Direct and technical — no filler phrases ("Great!", "Certainly!", etc.)
- Inline code references as clickable links where possible
- Raise a question rather than assume when requirements are genuinely ambiguous
- After completing a task, offer to update the wiki only if durable knowledge was produced
