# Wiki Index

Quick-reference hub for the `abbayedesmorts-java` project.
Read this first, then follow only the links relevant to your current task.

## Files

| File | Purpose |
|------|---------|
| [project.md](project.md) | What the project is, its goals, architecture, and key modules |
| [preferences.md](preferences.md) | Working standards, coding style, and AI interaction preferences |

## At a glance

- **Project**: Java/LWJGL3 port of the GPL game *Abbaye Des Morts* (originally C)
- **Status**: Mid-stage work-in-progress — collision detection, player control, enemy patrol/contact, bell trigger + altar hatch done; non-patrol enemy types and animation in progress; test baseline 165 passing, 3 skipped
- **Language**: Java 21, Maven build
- **Key pattern**: Loose Entity-Component System (ECS)
- **Formatting**: Google Java Format via Spotless (`mvn spotless:apply`)
- **Tests**: JUnit 5 + Mockito; game/collision tests run headless
