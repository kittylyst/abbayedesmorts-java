# enemies.txt Format Reference

Decoded from the original GPL C source:
- `src/enemies.c` (`searchenemies`, `drawenemies`, `movenemies`, and specialist functions)
- `src/loading.c` (`loaddata`)
- `src/structs.h` (`struct enem`)

---

## File Structure

```
X-Y                                       ← room header: column X, row Y (0-based)
f00 f01 f02 f03 f04 f05 f06 f07 f08 f09 f10 f11 f12 f13 f14   ← enemy slot 0
f00 f01 f02 f03 f04 f05 f06 f07 f08 f09 f10 f11 f12 f13 f14   ← enemy slot 1
...                                                             ← slots 2-6
X-Y                                       ← next room header
...
```

- 25 rooms total (5 columns × 5 rows, matching `Stage.SCREENS_X/Y`).
- **7 enemy slots** per room (indices 0–6).
- Each slot is one line of **15 three-digit decimal values** separated by spaces.
- A slot with `type == 0` means the slot is empty (all other fields are ignored).
- Room headers are consumed and skipped during parsing; order is row-major
  (room 0-0 first, then 0-1 … 0-4, then 1-0 … 4-4).

---

## Field Layout

The 15 fields per slot map directly to `struct enem` arrays (index `y` in C):

| Field | C field | Java name (proposed) | Meaning |
|------:|---------|----------------------|---------|
| 0 | `type[y]` | `type` | Enemy type code — see table below; 0 = absent |
| 1 | `x[y]` | `x` | Initial X position (pixels, C native resolution) |
| 2 | `y[y]` | `y` | Initial Y position (pixels, C native resolution) |
| 3 | `direction[y]` | `direction` | Initial movement direction: 0=right/2=up, 1=left/3=down |
| 4 | `tilex[y]` | `tileX` | Sprite atlas source X (pixels into `tiles.png`) |
| 5 | `tiley[y]` | `tileY` | Sprite atlas source Y (pixels into `tiles.png`) |
| 6 | `animation[y]` | `animation` | Initial animation frame index |
| 7 | `limleft[y]` | `limitLeft` | Left or upper patrol boundary (pixels) |
| 8 | `limright[y]` | `limitRight` | Right or lower patrol boundary (pixels); also used as water-surface Y for type 13 |
| 9 | `speed[y]` | `speed` | Movement speed (pixels/frame = `speed * 0.10`); reused as a counter for types 12, 13 |
| 10 | `fire[y]` | `fire` | Projectile/jump state flag; 0 initially |
| 11 | `adjustx1[y]` | `adjustX1` | Collision-box left-edge pixel adjustment |
| 12 | `adjustx2[y]` | `adjustX2` | Collision-box right-edge pixel adjustment |
| 13 | `adjusty1[y]` | `adjustY1` | Collision-box top-edge pixel adjustment |
| 14 | `adjusty2[y]` | `adjustY2` | Collision-box bottom-edge pixel adjustment |

**Coordinate system note:** C-source pixel coordinates use 8px-per-tile native resolution
(same as `Player.PIXELS_PER_TILE`). The Java port displays at 8× scale (`Stage.getTileSize()` = 64px),
so positions must be multiplied by 8 when placed in the Java world.

---

## Enemy Type Codes

Derived from `drawenemies`, `movenemies`, and the specialist functions (`crusaders`, `fireball`, etc.):

| Code | Name (proposed) | Sprite size | Movement | Notes |
|-----:|-----------------|-------------|----------|-------|
| 0 | *(absent)* | — | — | Slot is empty |
| 1 | `WALKER` | 16×16 | Horizontal patrol | Basic ground enemy |
| 2 | `WALKER_NO_FLIP` | 16×16 | Horizontal patrol | Does not flip sprite on turn (`tilex` unchanged) |
| 3 | `TALL_WALKER` | 16×24 | Horizontal patrol | 24px tall |
| 4 | `FLOATER_V` | 16×16 | Vertical patrol | `tilex` shifts 32px on direction change |
| 5 | `TALL_FLOATER_V` | 16×24 | Vertical patrol | `tilex` shifts 16px on direction change |
| 6 | `WIDE_WALKER` | 24×16 | Horizontal patrol | 24px wide; `tilex` shifts 48px on turn |
| 7 | `WALKER_7` | 16×16 | Horizontal patrol | Same as type 1 |
| 8 | `WALKER_8` | 16×16 | Horizontal patrol | Same as type 1 |
| 9 | `WALKER_9` | 16×16 | Horizontal patrol | Same as type 1 |
| 11 | `SHOOTER` | 16×24 | Stationary, fires | Fires projectile at Jean when `speed` reaches 50; slots 1–6 sync to slot 0 |
| 12 | `WATER_DROP` | 16×16 | Vertical fall loop | `speed` used as frame counter; `tilex` derived from counter; resets to `limleft` Y |
| 13 | `FIREBALL` | 16×16 | Vertical bounce | Rises, waits, falls; draws splash sprite at `limright` Y |
| 14 | `FISH` | 16×16 | Vertical bounce | Same logic as 13; no splash |
| 15 | `AIMING_SHOOTER` | 16×24 | Stationary, faces Jean | Flips sprite to track Jean; fires same as type 11 |
| 16 | `CRUSADER` | 16×24 | Marches right | `type=17` in file → converted to 16 at room load |
| 17 | `CRUSADER_SPAWN` | — | — | Sentinel in file; runtime initialises `x/y` and changes to 16 |
| 18 | `SATAN` | 32×24 | Vertical patrol, fires | Boss; room `ROOM_SATAN` only |
| 88 | `SMOKE` | 32×48 | Stationary | Animated smoke effect; `speed` drives frame |

Types 1–9 share the same `movenemies` patrol loop (directions 0–3) and two-frame animation
toggled at counter ticks 1, 11, 21.  Types 3, 5, and 15 use 24px height for collision;
type 6 uses 24px width.  All others are 16×16.

---

## Example: Room 0-2 (screen index 2), slot 0

```
001 056 144 000 128 040 000 056 224 005 000 001 013 006 015
```

| Field | Value | Interpretation |
|-------|-------|----------------|
| type | 1 | `WALKER` |
| x | 56 | 56 px (C) → 448 px (Java) |
| y | 144 | 144 px (C) → 1152 px (Java) |
| direction | 0 | facing right initially |
| tileX | 128 | atlas source X = 128 |
| tileY | 40 | atlas source Y = 40 |
| animation | 0 | frame 0 |
| limitLeft | 56 | left patrol boundary |
| limitRight | 224 | right patrol boundary |
| speed | 5 | 0.5 px/frame |
| fire | 0 | no projectile active |
| adjustX1 | 1 | |
| adjustX2 | 13 | |
| adjustY1 | 6 | |
| adjustY2 | 15 | |

---

## Java Parsing Notes

- The existing `Stage.load(String)` reads lines 4 chars wide (`k*4` to `k*4+3`).
  `enemies.txt` uses the **same** 4-char-per-field format (3 digits + space), and the
  same 7-rows-per-section structure with a header line per room.
- The C loader skips two header lines at the top of the file before the first room block.
  In `enemies.txt` the first line is `0-0` (the room header); there is no global file header
  to skip — the Java parser must consume the room header line itself.
- `loaddata` in C allocates `int enemydata[25][7][15]` which maps to
  `int[NUM_SCREENS][ENEMY_SLOTS][ENEMY_FIELDS]` in Java.
- `ENEMY_SLOTS = 7`, `ENEMY_FIELDS = 15`.
