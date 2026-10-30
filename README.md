# Fruity Frank - Custom Game Engine and 2D Puzzle Game

## Project Overview

This project consists of two major components:

1. A custom Java game engine (my initial contribution) implementing the MVCB (Model-View-Controller-Bot) architecture
2. Fruity Frank, a complete 2D action-puzzle game built on top of the engine

The engine was designed from scratch to support entity-component systems, GAL (Game Automaton Language) for AI behaviors, and flexible game configuration. The Fruity Frank game demonstrates the engine's capabilities with complex mechanics like two-layer terrain, apple physics, and dynamic enemy AI.

---

## Key Features

### Game Engine
- MVCB Architecture: Clean separation of Model, View, Controller, and Bot layers
- Entity System: Flexible component-based design for game objects
- GAL Support: Full integration with Game Automaton Language for defining AI behaviors
- Runtime Behavior Modification: Change entity behaviors by editing .gal files without recompiling
- Collision System: Rule-based collision detection and resolution
- Camera System: Smooth follow camera with configurable dead zones
- Configuration: All game parameters adjustable via external config files
- Debug Tools: Comprehensive debugging with performance metrics, hitboxes, and state visualization

### Fruity Frank Game
- Objective: Collect all fruits while avoiding monsters
- Innovative Terrain: Two-layer grass system (thick/thin) with realistic digging mechanics
- Apple Physics: Push apples horizontally, watch them fall and crush enemies
- Projectile System: Single cherry projectile that can eliminate monsters
- Dynamic Difficulty: Enemy AI adapts based on game progress
- Multiple Enemy Types: Tracker, Fleer, Patroller, Digger - each with unique GAL-defined behaviors
- Configurable: All game parameters adjustable via game.cfg

---

## Demo

Watch the full gameplay demo on Medal.tv: [https://medal.tv/games/imported-clips/clips/n02h6w0ygun8mt-cG?invite=cr-MSx5OWEsMjgwMDY4NjEy](https://medal.tv/games/imported-clips/clips/n02h6w0ygun8mt-cG?invite=cr-MSx5OWEsMjgwMDY4NjEy)

The demo showcases:
- Frank digging through the two-layer terrain
- Apple pushing and falling mechanics
- Enemy AI behaviors
- Projectile system
- Debug mode features

---

## Project Structure

```
ple2026/
├── README.md               # This file
├── LICENSE                 # MIT License
├── docs/
│   └── TECHNICAL_DETAILS.md # Complete technical documentation
├── game.cfg               # Game configuration
├── Makefile               # Build system
├── given.jar              # Runtime library
├── sprites/               # Graphics and audio assets
│   ├── Fruity_Frank-*.png
│   └── *.wav
├── gal_fsm/               # GAL automata files (AI behaviors)
│   ├── Tracking.gal
│   ├── Flee.gal
│   ├── AppleFall.gal
│   └── ...
├── src-engine/            # Custom game engine (my initial contribution)
│   └── engine/
│       ├── core/          # Model, Controller, Ticker
│       ├── entities/      # Entity, Bot, Stunt
│       ├── gal/           # GAL automata support
│       ├── geometry/      # Grid, ISU coordinate system
│       └── graphics/      # View, Camera, Painter
├── src-parser/            # GAL parser and compiler
│   └── gal/
│       ├── ast/           # AST nodes
│       ├── parser/        # JavaCC grammar
│       └── GalCompiler.java # Bridge between parser and engine
└── src-Frank/             # Fruity Frank game implementation
    └── frank/
        ├── game/          # Game-specific model, rules, config
        ├── entities/      # Frank, Enemy, Apple, Fruit, etc.
        ├── bots/          # FrankBot (player controller)
        ├── ai/            # FrankCategories (category matcher)
        └── gal/aut/       # Game-specific GAL actions/conditions
```

---

## Getting Started

### Prerequisites
- Java 17+
- JavaCC (for parser generation)
- JUnit 6 (optional, for tests)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/0ghassen0/fruity-frank.git
cd fruity-frank

# Build and run
make run

# For development (no JAR packaging)
make run-dev

# Clean build
make clean
make run
```

### Configuration
Edit game.cfg to customize game parameters:
- Map dimensions (cols, rows)
- Entity speeds (frank_speed, enemy_speed)
- Entity density ratios
- Viewport size
- Number of lives

---

## Enemy AI with GAL

All enemy behaviors are defined in GAL (Game Automaton Language) files:

| Automaton | Behavior | File |
|-----------|----------|------|
| Tracker | Pursues Frank directly | gal_fsm/Tracking.gal |
| Fleeing | Runs away from Frank | gal_fsm/Flee.gal |
| Square | Patrols in square pattern | gal_fsm/Square.gal |
| WallFollower | Follows existing paths | gal_fsm/Digger.gal |
| Random | Smart wandering | gal_fsm/EnemyRandom.gal |
| Spawner | Creates new enemies | gal_fsm/EnemySpawner.gal |
| AppleFall | Manages apple falling | gal_fsm/AppleFall.gal |
| Pickup | Handles fruit collection | gal_fsm/Pickup.gal |

**Key Innovation**: The GAL Compiler (src-parser/gal/ast/GalCompiler.java) bridges the teacher's GAL parser AST with our engine's executable automata, enabling runtime behavior modification.

---

## Controls

| Key | Action |
|-----|--------|
| Up Down Left Right | Move Frank (and dig) |
| SPACE | Fire cherry projectile |
| ESC | Pause / Resume |
| F3 | Toggle debug mode |

---

## Credits

**Engine and Initial Architecture**: [Ghassen Rouatbi](https://github.com/0ghassen0)

Originally developed as a university project (PLE 2026) at Universite Grenoble Alpes.

**Team Members**:
- Ghassen Rouatbi
- Gbedolo Morel Ahoueya
- Aya Hardabbou
- Djibrilla Elh Yaou Boureima
- Aziz Missaoui
- Samy Kherrat

**Assets**:
- Sprites: Authentic Amstrad CPC sprites from [The Spriters Resource](https://www.spriters-resource.com/)
- Original Game: Fruity Frank (Amstrad CPC)

---

## Documentation

- [Technical Details](docs/TECHNICAL_DETAILS.md) - Complete technical documentation including architecture, GAL integration, and implementation details
