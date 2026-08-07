# Code Review: `bob_phase_2` vs `main`

**Date:** 2026-08-07
**Branch:** `bob_phase_2` (`8621aaf`)
**Base:** `main` (`7a00bf2`)
**Scope:** Read-only review — no code changes
**Test baseline on branch:** 150 passing, 5 skipped (`mvn -Dspotless.check.skip=true test`)

## Summary

This branch ports enemy data loading, sprite rendering, horizontal patrol movement, hit-box contact detection, and room-transition enemy refresh. The pipeline (`enemies.txt` → `EnemyData` → `Enemy` → `Layer`) is clear and well tested for horizontal walkers and parsing fidelity.

Two gameplay-breaking gaps remain relative to the C source: crusader spawn markers are treated as live hazards, and vertical floaters (types 4–5) move on the wrong axis. Bugbot independently confirmed both highs plus a scaled-boundary fidelity issue.

## Branch contents (high level)

| Area | Change |
|------|--------|
| Model | `EnemyType` fully populated; new `EnemyData`; `Enemy` parse/build/update/render/hitBox |
| Stage | `loadEnemies`, `buildEnemies`, `refreshEnemies` on room moves / waypoints |
| Layer | Enemy update/render loops; `checkEnemyContact` |
| Player | `hitBox()`, `onEnemyContact()` |
| Docs | Added `docs/ENEMIES_FORMAT.md`; removed obsolete analysis/docs |
| Tests | `TestEnemyType`, `TestEnemyParsing`, `TestEnemyBehaviour` (38 new) |
| Build | Spotless / google-java-format version bump |

Commits on branch: `dd083e9` … `8621aaf` (6 commits).

---

## Findings

Sorted by severity (highest first). Sources: manual review and [Bugbot](1f233498-f223-4e7b-8be7-609f4eed9fc1).

| Severity | Location | Finding | Source |
|----------|----------|---------|--------|
| High | `Stage.java` (~161–165), `EnemyData.isPresent` | **Crusader spawn markers deal contact damage.** `buildEnemies()` instantiates `CRUSADER_SPAWN` (type 17) as live enemies that participate in `Layer.checkEnemyContact()`. C converts these in `searchenemies` and never treats them as hazards. Rooms 1-0 / 1-1 load seven spawns at `(0,0)` with adjust boxes that cover the top-left after scale → invisible kills / junk sprites. | Manual + Bugbot |
| High | `Enemy.java` (~148–210) | **Vertical patrol directions move horizontally.** Types 1–9 only move on X; every direction except `1` maps to `Facing.RIGHT`. C dirs 2/3 are vertical; `limitLeft`/`limitRight` are Y bounds for types 4–5. Map data (room 1-4 `TALL_FLOATER_V`, room 2-2 `FLOATER_V`) patrols the wrong axis → wrong trajectories and contact regions. | Manual + Bugbot |
| Medium | `Layer.checkEnemyContact` / `Player.onEnemyContact` | **No death / invulnerability state.** Contact can fire every tick while overlapping → multi-life drain (especially same-room respawn onto a hit box). C uses a death path; this only decrements lives and teleports. | Manual |
| Medium | `Enemy.java` (~196–204) | **Patrol boundary margin not scaled.** Boundary checks use hardcoded `±1` while positions, limits, and `patrolSpeed` are × `PIXELS_PER_TILE` (8). C’s ±1 native px should be ±8 display px. Walkers can overshoot limits and drift patrol / contact boxes. | Manual + Bugbot |
| Medium | `Enemy.render()` | **`WALKER_NO_FLIP` (type 2) still mirrors U when facing LEFT.** Docs/C say type 2 does not flip on turn. | Manual |
| Low | `EnemyType` | **Types 7–9 documented but missing from enum.** Not present in current `enemies.txt`, but `fromCode(7)` → `UNKNOWN` → silently dropped via `isPresent()`. | Manual |
| Low | `EnemyData.isPresent` | **Empty (`0`) and unrecognized codes both map to `UNKNOWN`.** Bad type codes vanish with no log. | Manual |
| Low | `Player.java` | **Death logic duplicated** between static-hazard path in `newPosition()` and `onEnemyContact()`. | Manual |
| Nit | `_context/wiki/project.md` | Checklist credits `TestGameDialog` / splash extract to this phase; that test already exists on `main`. | Manual |

### Bugbot-only table (compact)

| Severity | Location | Finding |
|----------|----------|---------|
| High | `Stage.java:161-165` | Crusader spawn markers deal contact damage |
| High | `Enemy.java:148-210` | Vertical patrol directions move horizontally |
| Medium | `Enemy.java:196-204` | Patrol boundary margin not scaled |

Bugbot found **3** issues (2 high, 1 medium). Manual review adds contact i-frames, type-2 flip, enum gaps, `isPresent` conflation, death-path duplication, and a wiki nit.

---

## What looks solid

- Parsing pinned to real `enemies.txt` values; coordinate scale (`× PIXELS_PER_TILE`) consistent for positions, limits, speed, and adjust offsets.
- Horizontal walker move + boundary reverse covered by `TestEnemyBehaviour`.
- Room transitions and waypoint teleports call `refreshEnemies()`; `Enemy.of(EnemyData)` calls `init()` so GL manager is set on rebuild.
- Hit boxes derive from C adjust fields; player body box documented against C dimensions.
- `docs/ENEMIES_FORMAT.md` is a useful authoritative reference; dead analysis docs cleaned up.

---

## Suggested fix order (for a follow-up; not done here)

1. Skip or convert `CRUSADER_SPAWN` before build/render/contact.
2. Four-way (or vertical) direction + Y patrol for types 4–5.
3. Death flag / brief invulnerability after enemy contact.
4. Scale boundary epsilon; exempt type 2 from U-flip.

---

## Diff stats

```
23 files changed, 1150 insertions(+), 1381 deletions(-)
```

Notable deletions are obsolete markdown under `docs/` and root analysis files; notable additions are enemy model/tests and `docs/ENEMIES_FORMAT.md`.
