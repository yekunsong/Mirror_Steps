# Mirror Steps

Mirror Steps is a 2D platform puzzle game built with JavaFX. The player moves through a series of stages that combine jumping, platform navigation, traps, pushable objects, portals, keys, doors, and other puzzle mechanics.

This repository is designed as a JavaFX game project with a clear separation between application routing, shared configuration, reusable entities, UI screens, and level-specific logic.

## Project Overview

Finished implementation includes:

- 14 playable levels
- Main menu, level selection screen, and settings screen
- Background music with volume control
- Pause menu with resume, menu, previous level, and next level actions
- A reusable base level framework
- Reusable gameplay entities for common platformer and puzzle mechanics

The game uses a fixed 1280x720 window size. Shared gameplay behavior is implemented in reusable classes, while each level keeps its own layout and special behavior isolated in its own package.

## Gameplay Features

Implemented gameplay systems include:

- Player movement, jumping, gravity, and respawn
- Sprint movement with `Shift`
- Pause and navigation overlay with `Esc`
- Static platforms and solid collision blocks
- Moving platforms
- Traps that reset the player
- Pushable blocks
- Floor buttons
- Linked portals
- Keys and doors
- Waterfall / water-flow entity logic
- Level-specific mechanics where needed

## Controls

- `A / Left Arrow`: move left
- `D / Right Arrow`: move right
- `W / Up Arrow / Space`: jump
- `Shift`: sprint
- `Esc`: pause / resume

Some levels may define extra controls for level-specific mechanics.

## Tech Stack and Tools

- Java: main programming language
- JavaFX: UI framework and game rendering
- JavaFX Media: background music playback
- Eclipse IDE: project development environment
- Git: version control
- GitHub: repository hosting and Pull Request workflow

The module declaration is in [`src/module-info.java`](src/module-info.java). The project currently requires:

- `javafx.controls`
- `javafx.graphics`
- `javafx.media`

## Architecture

The source code is organized into five main areas.

### `src/core`

- `Main.java`: JavaFX application entry point. It creates the shared configuration object and starts the router.
- `AppRouter.java`: Handles scene navigation, level loading, app closing, and background music initialization.
- `application.css`: Shared JavaFX styling.

### `src/config`

- `GameConfig.java`: Stores shared game configuration such as window size, player constants, physics values, colors, and keyboard mappings.

### `src/ui`

- `MenuView.java`: Builds the main menu scene.
- `LevelsView.java`: Builds the level selection scene.
- `SettingsView.java`: Builds the settings scene and controls music volume.

### `src/level`

- `BaseLevel.java`: Shared parent class for standard playable levels. It contains common scene setup, input handling, update loop, collision handling, pause menu logic, level navigation, and shared puzzle interactions.
- `level1` to `level14`: Concrete level packages. Each level owns its own layout and level-specific behavior.

### `src/entity`

Reusable gameplay objects include:

- `Player`
- `Block`
- `SolidBlock`
- `MovePlatform`
- `Trap`
- `PushableBlock`
- `FloorButton`
- `Portal`
- `Key`
- `Door`
- `Waterfall`

This structure keeps shared behavior in `BaseLevel` and `entity`, while keeping individual levels smaller and easier to modify independently.

## Project Structure

```text
Mirror_Steps/
├─ src/
│  ├─ config/
│  │  └─ GameConfig.java
│  ├─ core/
│  │  ├─ Main.java
│  │  ├─ AppRouter.java
│  │  └─ application.css
│  ├─ entity/
│  │  ├─ Player.java
│  │  ├─ Block.java
│  │  ├─ SolidBlock.java
│  │  ├─ MovePlatform.java
│  │  ├─ Trap.java
│  │  ├─ PushableBlock.java
│  │  ├─ Portal.java
│  │  ├─ FloorButton.java
│  │  ├─ Key.java
│  │  ├─ Door.java
│  │  └─ Waterfall.java
│  ├─ level/
│  │  ├─ BaseLevel.java
│  │  ├─ level1/
│  │  ├─ level2/
│  │  ├─ ...
│  │  └─ level14/
│  └─ ui/
│     ├─ MenuView.java
│     ├─ LevelsView.java
│     └─ SettingsView.java
├─ Pictures/
├─ Media/
├─ FRAMEWORK_GUIDE.md
├─ GITHUB_GUIDE.md
└─ README.md
```

## How to Run

### Option 1: Run in Eclipse

1. Clone this repository.
2. Import the project folder into Eclipse.
3. Add the JavaFX SDK to the project build path / module path.
4. Run [`src/core/Main.java`](src/core/Main.java).

Eclipse import path:

```text
File -> Import -> Projects from Folder or Archive -> Select Mirror_Steps
```

If JavaFX is not configured globally, add the JavaFX SDK manually before running the project.

### Option 2: Run from Command Line

Prerequisites:

- JDK installed
- JavaFX SDK installed
- Project already compiled
- Correct `--module-path` and `--add-modules` values for your local JavaFX SDK

Example:

```powershell
java --module-path "path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,javafx.media -cp bin core.Main
```

Adjust the JavaFX SDK path and classpath according to your local environment.

## Assets

Game assets are stored in:

- `Pictures/`: backgrounds, UI buttons, character sprites, platforms, doors, keys, and other image assets
- `Media/music.mp3`: background music

`AppRouter` initializes the music player when the application starts, and `SettingsView` controls the music volume.

## Development Notes

Recommended modification rules:

- Put single-level layout or behavior changes inside the relevant `levelX/LevelX.java` file.
- Put behavior shared by all standard levels in `BaseLevel.java`.
- Put reusable gameplay objects in `src/entity`.
- Put global constants and shared key bindings in `GameConfig.java`.
- Keep scene navigation inside `AppRouter`.

Shared files should be modified carefully because they can affect multiple levels or screens:

- `src/level/BaseLevel.java`
- `src/core/Main.java`
- `src/core/AppRouter.java`
- `src/config/GameConfig.java`
- `src/entity/Player.java`

## Team Collaboration

The repository includes [`FRAMEWORK_GUIDE.md`](FRAMEWORK_GUIDE.md) and [`GITHUB_GUIDE.md`](GITHUB_GUIDE.md) for project background and Git workflow notes.

Recommended workflow:

- Do not develop directly on `main`.
- Use one branch for one task.
- Submit finished work through a Pull Request.
- Discuss changes to shared framework files before editing them.
