# Mirror Steps Team Framework Guide

## 1. Goal

This project is organized so that one person can own the shared visual and audio direction, while each remaining team member can own one level independently.

The architecture separates:

- shared runtime code
- shared UI code
- shared game object code
- level-specific module code

The key design goal is to keep level work isolated. A team member should be able to modify their own level package without touching other levels or the core runtime.

## 2. Shared Parts

### `application`
This package contains startup code only.

- `Main`
  - Launches the JavaFX app.
  - Should stay minimal.

- `GameApp`
  - Creates global services.
  - Connects the scene manager to the runtime.

### `core`
This package controls application state and scene switching.

- `GameManager`
  - Stores shared runtime references.
  - Keeps track of the current level session and game state.

- `SceneManager`
  - Switches between menu, level select, and game scenes.
  - Should be the only place that decides which top-level scene is shown.

- `GameState`
  - Describes whether the game is in menu, level select, playing, or paused mode.

### `config`
This package contains reusable settings objects.

- `GameConfig`
  - Global window and control defaults.

- `ControlConfig`
  - Shared control mapping.

- `LevelConfig`
  - Per-level tuning and theme values.

### `entity`
This package contains reusable world objects.

- `GameObject`
  - Base renderable object.

- `Character`
  - Shared movement and spawn state for moving characters.

- `Player`
  - Player input and movement behavior.

- `Enemy`, `PatrolEnemy`
  - Enemy framework and one sample movement type.

- `Collectible`, `KeyItem`
  - Collectible framework.

- `Platform`, `MovingPlatform`, `Portal`
  - Core world objects that level mechanics can reuse.

### `ui`
This package contains non-gameplay screens.

- `MenuView`
  - Main menu.

- `LevelSelectView`
  - Level selection screen.

- `PauseView`
  - ESC overlay inside a level.

- `SettingsView`
  - Placeholder for audio, control, and visual settings.

### `audio`
This package contains the audio service.

- `AudioManager`
  - Shared music and sound effect facade.
  - Currently a placeholder, ready for real audio assets later.

## 3. Level Ownership Model

Each level is now treated as an independent module package:

- `level.level1`
- `level.level2`
- `level.level3`
- `level.level4`

Each level package should contain:

- one module class
- one mechanic class
- any helper classes needed only by that level

This is the part that makes team work easier.

### What a level module does

A level module defines:

- its id
- its config
- which mechanic implementation it uses

### What a level mechanic does

A level mechanic defines:

- how the level is built
- what special mechanic it uses
- any level-specific key handling
- optional per-frame logic

### Why this is better for team division

Each team member can work inside their own level package without editing the shared runtime.

For example, a level owner can change:

- platform layout
- enemy behavior
- world toggle logic
- portal locking logic
- unique interaction keys

without affecting the other levels.

## 4. Shared Runtime

### `level.api`
This package defines the contract between shared runtime and level-specific content.

- `LevelContext`
  - The API a mechanic uses to build and control its level.

- `LevelMechanic`
  - The behavior contract for a level.

- `LevelModule`
  - The definition of a playable level module.

### `level.runtime`
This package contains the shared game loop for levels.

- `LevelRuntime`
  - Handles the actual scene, pause overlay, collisions, player physics, and transition logic.
  - Should remain generic.
  - Level-specific content should not be coded directly here.

### `level.factory`
This package contains the level registry.

- `LevelFactory`
  - Returns the correct module for a selected level id.
  - Register new levels here when the team adds new stages.

## 5. How Team Members Should Work

### Visual and music owner
This person should mainly work on:

- `application.css`
- `AudioManager`
- `MenuView`
- `SettingsView`
- global theme values in `GameConfig`

### Level owners
Each of the four level owners should focus on their own level package only.

Recommended workflow for a level owner:

1. Edit the module class for the level.
2. Edit the mechanic class for the unique gameplay logic.
3. Add local helper classes only inside that level package if needed.
4. Register or update the level in `LevelFactory` if required.

## 6. What Should Stay Shared

These classes should not be rewritten by every level owner:

- `GameManager`
- `SceneManager`
- `LevelRuntime`
- `GameObject`
- `Character`
- `Player`
- `AudioManager`

They are shared infrastructure.

## 7. How To Add A New Level

1. Create a new package under `level`, for example `level.level5`.
2. Add a module class implementing `LevelModule`.
3. Add a mechanic class implementing `LevelMechanic`.
4. Put all level-specific content in that package.
5. Register the module in `LevelFactory`.

## 8. How To Add A Unique Mechanic

Mechanics are the main extension point.

Examples:

- mirror world toggle
- moving bridge
- key-and-door unlock
- enemy patrol route
- gravity switch

Use `build()` for static setup and `update()` / key hooks for the dynamic part.

## 9. Practical Rule For Editing

If a change is only relevant to one level, keep it inside that level package.

If a change is used by all levels, place it in the shared runtime or shared entity code.

