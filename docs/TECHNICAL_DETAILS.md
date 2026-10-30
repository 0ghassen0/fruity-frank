# Fruity Frank - Technical Documentation

## Table of Contents
1. [Game Rules](#game-rules)
2. [Architecture](#architecture)
3. [GAL Bridge](#gal-bridge)
4. [Entity Behaviors](#entity-behaviors)
5. [Project Structure](#project-structure)
6. [Compilation and Execution](#compilation-and-execution)
7. [Configuration](#configuration)
8. [Graphics and Audio](#graphics-and-audio)
9. [Technical Highlights](#technical-highlights)

---

## Game Rules

### Objective
Collect all fruits in the level without touching monsters. When the fruit counter reaches zero, the level is won.

### The Terrain: Two-Layer Grass
The world is entirely covered with grass initially. Each cell contains two superimposed layers:

| Layer | Thickness | Position | Destruction |
|-------|-----------|----------|-------------|
| GrassHigh | 9 cm | Upper part | Destroyed by any movement through it |
| GrassLow | 1 cm | Lower part | Destroyed only by vertical movement |

The world is toric horizontally (exiting right returns to left) and bounded vertically (solid walls at top and bottom).

### Digging Galleries
- Frank digs by moving: he transforms grass into galleries
- Horizontal movement: Only destroys thick grass (thin grass remains as gallery floor)
- Vertical movement: Destroys both layers
- Digging monsters also dig; non-digging monsters are confined to existing galleries

### Fruits and Cherries
Collected by Frank passing over them. Each collection:
- Increases score
- Decreases remaining fruit counter
- Cherries additionally recharge Frank's projectile

### Apples
- Pushing: Frank pushes apples horizontally, one cell at a time, as long as the cell behind the apple is free (empty or grass)
- Falling: An apple falls when unsupported (the cell below no longer contains thick grass), after a short delay
- When falling, apples:
  - Crush thin grass
  - Kill Frank (lose a life)
  - Destroy monsters
  - Stop on solid obstacles

### Projectile
Frank has one projectile at a time:
- Recovered by collecting a cherry
- Fired with SPACE in Frank's facing direction
- Destroys the first monster hit, then disappears
- Must collect another cherry to shoot again

### Lives and Game End
- Frank has 3 lives (configurable)
- After death, Frank temporarily becomes invincible to prevent death chains
- Game ends in game over (no lives left) or victory (all fruits collected)

---

## Architecture

### MVCB Pattern

```
INPUT (Keyboard/Mouse)
     |
     v
+--------------+
| FrankController |  Captures input, manages menus, transmits to player Bot
+--------------+
     |
     v
+--------+--------+
|    BOT     |  Decides actions:
|            |    - FrankBot -> keyboard (Frank)
|            |    - GALBot -> GAL automaton (monsters, apples, fruits, spawner)
+--------------+
     | pilot
     v
+------------------+
|      MODEL        |  Game state: entities, physics, collision rules
| FruityFrankModel  |  Knows nothing about display or input
|   (+ Entities)     |
+------------------+
     | read by
     v
+--------+--------+
|    VIEW    |  Reads Model and draws it; never modifies it
| FrankView  |  (following camera, avatars, HUD)
+--------------+
     |
     v
   SCREEN
```

The Model is the logical core and ignores display and keyboard. The View observes the Model and draws it without modification. This separation allows independent evolution of logic and rendering.

### Class Distribution

| Component | Classes |
|-----------|---------|
| Model | FruityFrankModel, Rules, GameState, FruityFrankMap |
| Entities | Frank, Enemy, Fruit, Apple, Cherry, CherryProjectile, GrassHigh, GrassLow, EnemySpawnerEntity |
| View | FrankView, FrankAvatar, FrankPainter, FrankSprites, Palette |
| Controller | FrankController (input, menus, pause, restart) |
| Bot/AI | FrankBot (player), GALBot + .gal automata, FrankCategories |
| GAL Bridge | GalCompiler + actions/conditions in frank.gal.aut.* |
| Audio | FrankAudio |
| Configuration | FruityFrank, Category, Categorized, GameConfig |
| Assembly | GameApp (entry point) |

---

## GAL Bridge: From .gal Files to Executable Automata

This is the centerpiece of the project. The complete flow:

```
.gal file
     |
     v Parser.from_file()  (teacher's provided parser)
AST  (gal.ast.Automaton, Mode, Transition, Condition, FunCall...)
     |
     v GalCompiler.compile()
Executable Automaton  (engine.gal.aut.Automaton)
     |
     v new GALBot(entity, automaton, speed, categories)
Bot that controls the entity each tick
```

GalCompiler (in src-parser/gal/ast/GalCompiler.java) translates each AST node from the parser into executable classes for the engine:

- GAL conditions (Closest, Cell/Step, Got, MyDir, Struck->CanFall) become iGALCondition in frank.gal.aut.conditions
- GAL actions (Move, Turn, Hit, Egg, Explode, Pick, Wait) become iGALAction in frank.gal.aut.actions
- Special states are handled: () -> State.DESTROY (entity destroys itself), (_) -> State.RANDOM (random target state)

At startup, GameApp.loadGalAutomata() loads all files from gal_fsm/ into a name -> Automaton table, which entities use to get their behavior.

**Important architectural point**: The ability to dig is not in the automaton. The automaton only says "go towards Frank" or "flee"; it's the Model that decides if this movement crosses (and digs) the grass, based on Enemy.canDig(). Thus, the same .gal file produces different visual behaviors depending on whether the enemy can dig or not.

---

## Entity Behaviors

All .gal files are in the gal_fsm/ directory.

| GAL File | Automaton | Role |
|----------|-----------|------|
| Tracking.gal | Tracker | Pursues Frank by aiming for the most direct direction, avoids obstacles |
| Flee.gal | Fleeing | Flees from Frank by moving in the opposite direction |
| Square.gal | Square | Patrols in a square, then switches to attack mode when Frank is close |
| Digger.gal | WallFollower | Follows galleries (and creates them if it can dig) |
| EnemyRandom.gal | EnemyRandom | "Intelligent" wandering: pursues if Frank is close, otherwise wanders |
| AppleFall.gal | AppleFall | Manages apple falling (condition Struck/CanFall, action Move(S)) |
| Pickup.gal | Pickup | Allows a fruit/cherry to be collected when Frank covers it, then self-destructs |
| EnemySpawner.gal | EnemySpawner | Periodically generates new monsters around the spawn point |

### Enemy Spawner
Monsters are created by a dedicated entity, EnemySpawnerEntity, controlled by EnemySpawner.gal. At regular intervals (Wait(100)), the automaton executes the Egg action on an adjacent free cell to spawn a new monster.

A factory (defined in GameApp) assigns each spawned monster a type and automaton: as long as more than half of the fruits remain, monsters receive varied behaviors (patrol, wandering, wall follower); below this threshold, all switch to Tracker mode to increase difficulty at the end of the level.

---

## Project Structure

```
ple2026/
├── Makefile                       # Build, run, and tests (JavaCC + javac + jar)
├── game.cfg                       # Game configuration (modifiable without recompiling)
├── gal_fsm/                       # GAL automata for entities
│   ├── Tracking.gal
│   ├── Flee.gal
│   ├── Square.gal
│   ├── Digger.gal
│   ├── EnemyRandom.gal
│   ├── AppleFall.gal
│   ├── Pickup.gal
│   └── EnemySpawner.gal
├── sprites/                       # Sprites, sounds, and menu screens
│   ├── Fruity_Frank-player.png    # Frank
│   ├── Fruity_Frank-enemies.png   # Monsters
│   ├── Fruity_Frank-Items.png     # Fruits, apple, cherry
│   ├── jon.wav                    # Background music
│   ├── eat.wav, death.wav, explosion.wav, gun-shot.wav, win.wav, game-over.wav
│   └── fruity-frank_start.png, echap_menu.png, *_restart_exit.png
├── src-engine/engine/             # Generic MVCB engine
│   ├── ai/, core/, debug/, entities/, gal/, geometry/, graphics/, shapes/, tests/
├── src-parser/gal/                # GAL parser + AST + GalCompiler (the bridge)
│   ├── ast/, demo/, parser/, util/
│   └── parser/parser.jj           # JavaCC grammar (generated at compile time)
├── src-Frank/frank/               # Game code
│   ├── game/                      # Model, config, rules, controller, audio, main
│   ├── entities/                  # All entities
│   ├── bots/                      # FrankBot (keyboard)
│   ├── ai/                        # FrankCategories
│   ├── gal/aut/                   # GAL actions & conditions (the bridge)
│   │   ├── actions/               # Move, Turn, Hit, Egg, Explode, Pick
│   │   └── conditions/            # Closest, Step, CanFall, Got, MyDir
│   └── graphics/                  # View, Avatar, Painter, Sprites, Palette
├── src-pacman/game/               # Reference PacMan game (provided)
├── lib/                           # Optional dependencies (JUnit 6 for src-pacman & tests)
├── bin/                           # Build output (classes + fruity-frank.jar)
└── given.jar                      # oop library (runtime, graphics, sound)
```

---

## Compilation and Execution

### With Makefile (Recommended)
A Makefile automates the entire chain: JavaCC parser generation, compilation, JAR packaging, and execution.

| Target | Effect |
|--------|--------|
| make / make all | Builds the executable JAR (bin/fruity-frank.jar) |
| make parser | Generates parser sources with JavaCC |
| make compile | Compiles all sources in bin/classes/ |
| make jar | Creates the executable JAR (includes sprites/ and game.cfg) |
| make run | Compiles, packages, then runs the game via JAR |
| make run-dev | Runs the game directly from classpath (no JAR) |
| make test | Executes EngineTest with JUnit 6 (ignored if JUnit absent) |
| make clean | Removes build artifacts |
| make help | Displays help listing all targets |

```bash
# Build and run in one command
make run

# Fast development, without JAR packaging
make run-dev

# Clean build
make clean
```

The build chain automatically runs parser -> compile -> jar, so make run regenerates the parser and recompiles as needed. The JAR includes the manifest (Main-Class: frank.game.GameApp, Class-Path: given.jar), the sprites/ directory, and game.cfg; the run target also copies given.jar next to the JAR for Class-Path resolution.

> Note: The game loads its resources (sprites/, game.cfg, gal_fsm/) from the current working directory. Run it from the project root.

### In Eclipse
1. Import the project and add given.jar to libraries
2. Verify that game.cfg, gal_fsm/, and sprites/ are at the project root
3. Run the frank.game.GameApp class

### Command Line (Without Makefile)
```bash
# Default configuration (game.cfg at root)
java frank.game.GameApp

# Specific configuration passed as argument
java frank.game.GameApp configs/large_map.cfg
```

If no argument is given, the game loads game.cfg. Otherwise, it loads the specified file.

---

## Configuration File

game.cfg allows modifying the game without recompiling. Format: key = value, one per line; empty lines and those starting with # are ignored.

| Key | Effect |
|-----|--------|
| cols, rows | Map size |
| frank_speed, enemy_speed | Movement speeds |
| fruit_ratio, apple_ratio, cherry_ratio | Higher = fewer elements (1 per N cells) |
| spawnerx, spawnery | Spawner position; its entire row and column are cleared at start |
| view_cols, view_rows | Window dimensions |
| frank_lives | Initial number of lives |

To test a large, sparse map, simply increase cols/rows and the *_ratio values—no code changes needed.

---

## Graphics and Audio

### Sprites
Authentic Amstrad CPC sprites (source: [The Spriters Resource](https://www.spriters-resource.com/)). Spritesheets follow a regular 20x15 px grid, origin at (4, 5), step 24x20.

| Sheet | Dimensions | Content |
|-------|------------|---------|
| Fruity_Frank-player.png | 100x76 | Frank (3 directions x 4 frames) |
| Fruity_Frank-enemies.png | 148x136 | 4 monsters (orange, purple, green, red) |
| Fruity_Frank-Items.png | 124x64 | Fruits, apple, cherry |

Rendering respects sprite ratios to prevent distortion: Frank stands on the ground, monsters float in galleries, animated according to their actual direction.

### Audio (FrankAudio)
Each event triggers a sound effect over the background music:

| Sound | Event |
|-------|--------|
| jon.wav | Background music (loop) |
| eat.wav | Fruit or cherry collection |
| gun-shot.wav | Projectile firing |
| explosion.wav | Monster destruction |
| death.wav | Frank loses a life |
| win.wav | Victory |
| game-over.wav | Game over |

---

## Technical Highlights

### Demo Points
The demonstration highlights two key technical points:

1. Apple Falling Management with the two-layer system (thick grass/thin grass):
   - An apple remains stable on thick grass
   - Falls when unsupported
   - Crushes thin grass and kills what's below

2. Apple Pushing by Frank:
   - Horizontal pushing, cell by cell
   - Stops against obstacles

Other implemented challenges: gallery digging, fruit collection and victory condition, single rechargeable projectile, and monster AI controlled by interchangeable GAL automata.

### Performance
- Target FPS: 30 (MOTION_TPS)
- AI Updates: 8 per second (AI_TPS)
- Spawn Updates: 2 per second (SPAWN_TPS)
- Render Target: 33ms (~30 FPS)
- Optimizations: Active region simulation, grid-based collision detection

### Debug Mode (F3)
- Time between ticks (min, max, average)
- Time between paints (min, max, average)
- FPS (min, max, average)
- Load testing with entity spawner
- Display current action above entities
- Bounding box visualization
